package com.example.studyblocks.repository

import com.example.studyblocks.data.local.dao.StudyBlockDao
import com.example.studyblocks.data.local.dao.StudySessionDao
import com.example.studyblocks.data.local.dao.SubjectDao
import com.example.studyblocks.data.local.dao.UserDao
import com.example.studyblocks.data.model.*
import com.example.studyblocks.scheduling.StudyScheduler
import com.example.studyblocks.data.model.XPManager
import com.example.studyblocks.sync.FirebaseSyncRepository
import com.example.studyblocks.sync.SyncResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudyRepository @Inject constructor(
    private val userDao: UserDao,
    private val subjectDao: SubjectDao,
    private val studyBlockDao: StudyBlockDao,
    private val studySessionDao: StudySessionDao,
    private val studyScheduler: StudyScheduler,
    private val firebaseSyncRepository: FirebaseSyncRepository,
    private val xpManager: XPManager
) {
    
    // User operations
    suspend fun getCurrentUser(): User? = userDao.getCurrentUser()
    fun getCurrentUserFlow(): Flow<User?> = userDao.getCurrentUserFlow()
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)
    
    // Subject operations
    fun getAllSubjects(userId: String): Flow<List<Subject>> = subjectDao.getAllSubjects(userId)
    suspend fun getSubjectById(id: String): Subject? = subjectDao.getSubjectById(id)
    suspend fun insertSubject(subject: Subject) = subjectDao.insertSubject(subject)
    suspend fun updateSubject(subject: Subject) = subjectDao.updateSubject(subject)
    suspend fun deleteSubject(subject: Subject) {
        // Delete associated blocks first
        studyBlockDao.deleteBlocksForSubject(subject.id)
        subjectDao.deleteSubject(subject)
    }
    
    suspend fun updateSubjectConfidence(subjectId: String, confidence: Int) {
        subjectDao.updateSubjectConfidence(subjectId, confidence, LocalDateTime.now())
    }
    
    // Study Block operations
    fun getBlocksForDate(userId: String, date: LocalDate): Flow<List<StudyBlock>> =
        studyBlockDao.getBlocksForDate(userId, date)
    
    fun getBlocksForDateRange(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<StudyBlock>> =
        studyBlockDao.getBlocksForDateRange(userId, startDate, endDate)
    
    fun getBlocksForSubject(subjectId: String): Flow<List<StudyBlock>> =
        studyBlockDao.getBlocksForSubject(subjectId)
    
    suspend fun getBlockById(id: String): StudyBlock? = studyBlockDao.getBlockById(id)
    
    suspend fun insertStudyBlock(block: StudyBlock) = studyBlockDao.insertBlock(block)
    
    suspend fun markBlockComplete(blockId: String): Int {
        val completedBlock = studyBlockDao.getBlockById(blockId)
        if (completedBlock != null) {
            val subject = subjectDao.getSubjectById(completedBlock.subjectId)
            val user = userDao.getCurrentUser()
            
            if (subject != null && user != null) {
                val totalBlocks = studyBlockDao.getBlocksForSubject(completedBlock.subjectId).first().size
                val completedBlocks = studyBlockDao.getBlocksForSubject(completedBlock.subjectId).first().count { it.isCompleted }
                
                val blockXP = xpManager.calculateBlockXP(completedBlocks + 1, totalBlocks, subject)
                val updatedSubject = xpManager.updateSubjectXP(subject, blockXP)
                
                // Get all subjects for the user to calculate global XP
                val allSubjects = subjectDao.getAllSubjects(user.id).first().map { 
                    if (it.id == updatedSubject.id) updatedSubject else it
                }
                val updatedUser = xpManager.updateUserGlobalXP(user, allSubjects)
                
                subjectDao.updateSubjectXP(updatedSubject.id, updatedSubject.xp, updatedSubject.level, LocalDateTime.now())
                userDao.updateUserGlobalXP(updatedUser.id, updatedUser.globalXp, updatedUser.globalLevel, LocalDateTime.now())
                
                studyBlockDao.markBlockComplete(blockId, true, LocalDateTime.now())
                return blockXP
            }
        }
        
        studyBlockDao.markBlockComplete(blockId, true, LocalDateTime.now())
        return 0
    }
    
    suspend fun markBlockIncomplete(blockId: String): Int {
        val incompletedBlock = studyBlockDao.getBlockById(blockId)
        if (incompletedBlock != null && incompletedBlock.isCompleted) {
            val subject = subjectDao.getSubjectById(incompletedBlock.subjectId)
            val user = userDao.getCurrentUser()
            
            if (subject != null && user != null) {
                val totalBlocks = studyBlockDao.getBlocksForSubject(incompletedBlock.subjectId).first().size
                val completedBlocks = studyBlockDao.getBlocksForSubject(incompletedBlock.subjectId).first().count { it.isCompleted }
                
                // Calculate the XP that was awarded for this block and subtract it
                val blockXP = xpManager.calculateBlockXP(completedBlocks, totalBlocks, subject)
                val updatedSubject = xpManager.updateSubjectXP(subject, -blockXP)
                
                // Get all subjects for the user to calculate global XP
                val allSubjects = subjectDao.getAllSubjects(user.id).first().map { 
                    if (it.id == updatedSubject.id) updatedSubject else it
                }
                val updatedUser = xpManager.updateUserGlobalXP(user, allSubjects)
                
                subjectDao.updateSubjectXP(updatedSubject.id, updatedSubject.xp, updatedSubject.level, LocalDateTime.now())
                userDao.updateUserGlobalXP(updatedUser.id, updatedUser.globalXp, updatedUser.globalLevel, LocalDateTime.now())
                
                studyBlockDao.markBlockComplete(blockId, false, null)
                return blockXP
            }
        }
        
        studyBlockDao.markBlockComplete(blockId, false, null)
        return 0
    }
    
    // Schedule Generation
    suspend fun generateNewSchedule(
        userId: String,
        preferredBlocksPerDay: Int? = null,
        scheduleHorizon: Int? = null,
        blockDurationMinutes: Int? = null
    ): SchedulingResult {
        val user = userDao.getCurrentUser()
        val subjects = subjectDao.getAllSubjects(userId).first()
        
        val actualHorizon = scheduleHorizon ?: 21
        
        if (subjects.isEmpty()) {
            return SchedulingResult(
                blocks = emptyList(),
                totalBlocks = 0,
                scheduleHorizon = actualHorizon,
                averageBlocksPerDay = 0.0,
                subjectDistribution = emptyMap()
            )
        }
        
        // Clear existing pending blocks
        studyBlockDao.deletePendingBlocksForUser(userId)
        
        // Generate new schedule with specified preferences
        val actualPreferredBlocks = preferredBlocksPerDay ?: user?.preferredBlocksPerDay ?: 3
        val actualBlockDuration = blockDurationMinutes ?: 60
        val newBlocks = studyScheduler.generateSchedule(
            subjects = subjects,
            userId = userId,
            scheduleHorizon = actualHorizon,
            preferredBlocksPerDay = actualPreferredBlocks,
            blockDurationMinutes = actualBlockDuration
        )
        
        // Insert new blocks
        studyBlockDao.insertBlocks(newBlocks)
        
        // Return scheduling result
        return SchedulingResult(
            blocks = newBlocks,
            totalBlocks = newBlocks.size,
            scheduleHorizon = actualHorizon,
            averageBlocksPerDay = newBlocks.size / actualHorizon.toDouble(),
            subjectDistribution = newBlocks.groupingBy { it.subjectName }.eachCount()
        )
    }
    
    // Study Session operations
    fun getAllSessions(userId: String): Flow<List<StudySession>> = studySessionDao.getAllSessions(userId)
    
    fun getActiveSession(userId: String): Flow<StudySession?> = studySessionDao.getActiveSessionFlow(userId)
    
    suspend fun startStudySession(studyBlockId: String, subjectId: String, userId: String, plannedDuration: Int): StudySession {
        val session = StudySession(
            id = java.util.UUID.randomUUID().toString(),
            studyBlockId = studyBlockId,
            subjectId = subjectId,
            userId = userId,
            startTime = LocalDateTime.now(),
            plannedDurationMinutes = plannedDuration
        )
        studySessionDao.insertSession(session)
        return session
    }
    
    suspend fun endStudySession(sessionId: String, focusScore: Int? = null): StudySession? {
        val session = studySessionDao.getSessionById(sessionId) ?: return null
        val endTime = LocalDateTime.now()
        val actualDuration = java.time.temporal.ChronoUnit.MINUTES.between(session.startTime, endTime).toInt()
        
        val updatedSession = session.copy(
            endTime = endTime,
            actualDurationMinutes = actualDuration,
            isCompleted = true,
            focusScore = focusScore
        )
        
        studySessionDao.updateSession(updatedSession)
        return updatedSession
    }
    
    // Analytics
    suspend fun getTotalStudyMinutes(userId: String): Int = studySessionDao.getTotalStudyMinutes(userId) ?: 0
    suspend fun getTotalCompletedBlocks(userId: String): Int = studyBlockDao.getTotalCompletedBlocks(userId)
    suspend fun getCompletedBlocksForDate(userId: String, date: LocalDate): Int = 
        studyBlockDao.getCompletedBlocksForDate(userId, date)
        
    fun getTotalStudyMinutesFlow(userId: String): Flow<Int> = kotlinx.coroutines.flow.flow {
        emit(getTotalStudyMinutes(userId))
    }
    
    fun getTotalCompletedBlocksFlow(userId: String): Flow<Int> = kotlinx.coroutines.flow.flow {
        emit(getTotalCompletedBlocks(userId))
    }
    
    suspend fun deleteAllDataForUser(userId: String) {
        // Delete from Firebase first
        firebaseSyncRepository.deleteUserDataFromFirestore(userId)
        
        // Delete all user-related data locally
        studySessionDao.deleteAllSessionsForUser(userId)
        studyBlockDao.deleteAllBlocksForUser(userId)
        subjectDao.deleteAllSubjectsForUser(userId)
        userDao.deleteUserById(userId)
    }
    
    // Sync operations
    suspend fun syncUserData(userId: String): SyncResult {
        return firebaseSyncRepository.syncUserData(userId)
    }
    
    suspend fun uploadDataToCloud(userId: String): SyncResult {
        return firebaseSyncRepository.syncUserData(userId)
    }
}

data class SchedulingResult(
    val blocks: List<StudyBlock>,
    val totalBlocks: Int,
    val scheduleHorizon: Int,
    val averageBlocksPerDay: Double,
    val subjectDistribution: Map<String, Int>
)