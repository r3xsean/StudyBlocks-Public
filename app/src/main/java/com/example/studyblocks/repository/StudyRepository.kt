package com.example.studyblocks.repository

import androidx.room.Transaction
import com.example.studyblocks.data.local.dao.StudyBlockDao
import com.example.studyblocks.data.local.dao.StudySessionDao
import com.example.studyblocks.data.local.dao.SubjectDao
import com.example.studyblocks.data.local.dao.UserDao
import com.example.studyblocks.data.local.dao.SchedulePreferencesDao
import com.example.studyblocks.data.model.*
import com.example.studyblocks.scheduling.StudyScheduler
import com.example.studyblocks.data.model.XPManager
import com.example.studyblocks.sync.FirebaseSyncRepository
import com.example.studyblocks.sync.SyncResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val schedulePreferencesDao: SchedulePreferencesDao,
    private val studyScheduler: StudyScheduler,
    private val firebaseSyncRepository: FirebaseSyncRepository,
    private val xpManager: XPManager
) {
    
    // Mutex to prevent concurrent block completion operations 
    private val blockCompletionMutex = Mutex()
    
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
    
    // Schedule Preferences operations
    suspend fun getSchedulePreferences(userId: String): SchedulePreferences? = 
        schedulePreferencesDao.getSchedulePreferences(userId)
    
    fun getSchedulePreferencesFlow(userId: String): Flow<SchedulePreferences?> = 
        schedulePreferencesDao.getSchedulePreferencesFlow(userId)
    
    suspend fun insertSchedulePreferences(preferences: SchedulePreferences) = 
        schedulePreferencesDao.insertSchedulePreferences(preferences)
    
    suspend fun updateSchedulePreferences(preferences: SchedulePreferences) = 
        schedulePreferencesDao.updateSchedulePreferences(preferences)
    
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
        return blockCompletionMutex.withLock {
            performAtomicBlockCompletion(blockId, true)
        }
    }
    
    @Transaction
    private suspend fun performAtomicBlockCompletion(blockId: String, isComplete: Boolean): Int {
        // Get current block state atomically
        val block = studyBlockDao.getBlockById(blockId) ?: return 0
        
        // Prevent duplicate operations - if already in desired state, return 0
        if (block.isCompleted == isComplete) {
            return 0
        }
        
        val subject = subjectDao.getSubjectById(block.subjectId) ?: return 0
        val user = userDao.getCurrentUser() ?: return 0
        
        // Get current completion data in one atomic read
        val allBlocksForSubject = studyBlockDao.getBlocksForSubject(block.subjectId).first()
        val allSubjects = subjectDao.getAllSubjects(user.id).first()
        
        // Calculate total time allocated to this subject and total subject count
        val totalSubjectTimeMinutes = allBlocksForSubject.sumOf { it.durationMinutes }
        val totalSubjectCount = allSubjects.size
        
        // Calculate XP for this specific block - each subject gets equal total XP
        val blockXP = xpManager.calculateBlockXP(
            blockDurationMinutes = block.durationMinutes,
            totalSubjectTimeMinutes = totalSubjectTimeMinutes,
            totalSubjectCount = totalSubjectCount,
            subject = subject
        )
        
        // Calculate XP changes
        val xpDelta = if (isComplete) blockXP else -blockXP
        val updatedSubject = xpManager.updateSubjectXP(subject, xpDelta)
        
        // Update global XP calculation
        val allUpdatedSubjects = subjectDao.getAllSubjects(user.id).first().map { 
            if (it.id == updatedSubject.id) updatedSubject else it
        }
        val updatedUser = xpManager.updateUserGlobalXP(user, allUpdatedSubjects)
        
        // Perform all database updates atomically
        subjectDao.updateSubjectXP(updatedSubject.id, updatedSubject.xp, updatedSubject.level, LocalDateTime.now())
        userDao.updateUserGlobalXP(updatedUser.id, updatedUser.globalXp, updatedUser.globalLevel, LocalDateTime.now())
        studyBlockDao.markBlockComplete(blockId, isComplete, if (isComplete) LocalDateTime.now() else null)
        
        return blockXP
    }
    
    suspend fun markBlockIncomplete(blockId: String): Int {
        return blockCompletionMutex.withLock {
            performAtomicBlockCompletion(blockId, false)
        }
    }
    
    // Missed Block Rescheduling
    suspend fun rescheduleWithMissedBlocks(userId: String): SchedulingResult {
        val allBlocks = studyBlockDao.getAllBlocksForUser(userId).first()
        val schedulePrefs = getSchedulePreferences(userId)
        
        val actualBlocksPerWeekday = schedulePrefs?.blocksPerWeekday ?: 3
        val actualBlocksPerWeekend = schedulePrefs?.blocksPerWeekend ?: 2
        val actualHorizon = schedulePrefs?.scheduleHorizonDays ?: 21
        
        // Get rescheduled blocks
        val rescheduledBlocks = studyScheduler.rescheduleWithMissedBlocks(
            allBlocks = allBlocks,
            userId = userId,
            blocksPerWeekday = actualBlocksPerWeekday,
            blocksPerWeekend = actualBlocksPerWeekend,
            scheduleHorizon = actualHorizon
        )
        
        // Delete existing incomplete blocks and insert rescheduled ones
        studyBlockDao.deletePendingBlocksForUser(userId)
        
        // Only insert the non-completed blocks (completed blocks maintain their original state)
        val blocksToInsert = rescheduledBlocks.filter { !it.isCompleted }
        studyBlockDao.insertBlocks(blocksToInsert)
        
        return SchedulingResult(
            blocks = rescheduledBlocks,
            totalBlocks = rescheduledBlocks.size,
            scheduleHorizon = actualHorizon,
            averageBlocksPerDay = rescheduledBlocks.filter { !it.isCompleted }.size / actualHorizon.toDouble(),
            subjectDistribution = rescheduledBlocks.groupingBy { it.subjectName }.eachCount()
        )
    }
    
    // Schedule Generation
    suspend fun generateNewSchedule(
        userId: String,
        blocksPerWeekday: Int? = null,
        blocksPerWeekend: Int? = null,
        scheduleHorizon: Int? = null,
        blockDurationMinutes: Int? = null
    ): SchedulingResult {
        val user = userDao.getCurrentUser()
        val subjects = subjectDao.getAllSubjects(userId).first()
        val schedulePrefs = getSchedulePreferences(userId)
        
        val actualHorizon = scheduleHorizon ?: schedulePrefs?.scheduleHorizonDays ?: 21
        
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
        val actualBlocksPerWeekday = blocksPerWeekday ?: schedulePrefs?.blocksPerWeekday ?: 3
        val actualBlocksPerWeekend = blocksPerWeekend ?: schedulePrefs?.blocksPerWeekend ?: 2
        val actualBlockDuration = blockDurationMinutes ?: schedulePrefs?.defaultBlockDurationMinutes ?: 60
        val newBlocks = studyScheduler.generateSchedule(
            subjects = subjects,
            userId = userId,
            scheduleHorizon = actualHorizon,
            blocksPerWeekday = actualBlocksPerWeekday,
            blocksPerWeekend = actualBlocksPerWeekend,
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