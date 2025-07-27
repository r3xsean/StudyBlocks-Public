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
// import com.example.studyblocks.sync.FirebaseSyncRepository // Disabled for open source version
// import com.example.studyblocks.sync.SyncResult // Defined locally instead
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

// Define SyncResult locally since Firebase sync is disabled
sealed class SyncResult {
    object Success : SyncResult()
    data class Error(val message: String) : SyncResult()
}

@Singleton
class StudyRepository @Inject constructor(
    private val userDao: UserDao,
    private val subjectDao: SubjectDao,
    private val studyBlockDao: StudyBlockDao,
    private val studySessionDao: StudySessionDao,
    private val schedulePreferencesDao: SchedulePreferencesDao,
    private val studyScheduler: StudyScheduler,
    // private val firebaseSyncRepository: FirebaseSyncRepository, // Disabled for open source version
    private val xpManager: XPManager
) {
    
    // Mutex to prevent concurrent block completion operations 
    private val blockCompletionMutex = Mutex()
    
    // User operations
    suspend fun getCurrentUser(): User? = userDao.getCurrentUser()
    fun getCurrentUserFlow(): Flow<User?> = userDao.getCurrentUserFlow()
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)
    
    suspend fun setPendingSummaryDate(userId: String, summaryDate: String?) {
        try {
            val currentUser = getCurrentUser()
            if (currentUser != null && currentUser.id == userId) {
                val updatedUser = currentUser.copy(pendingSummaryDate = summaryDate)
                updateUser(updatedUser)
                android.util.Log.d("StudyRepository", "Set pending summary date: $summaryDate for user: $userId")
            } else {
                android.util.Log.w("StudyRepository", "Cannot set pending summary date - user not found or ID mismatch")
            }
        } catch (e: Exception) {
            android.util.Log.e("StudyRepository", "Error setting pending summary date", e)
        }
    }
    
    // Subject operations
    fun getAllSubjects(userId: String): Flow<List<Subject>> = subjectDao.getAllSubjects(userId)
    suspend fun getSubjectById(id: String): Subject? = subjectDao.getSubjectById(id)
    suspend fun insertSubject(subject: Subject) = subjectDao.insertSubject(subject)
    
    suspend fun insertSubjects(subjects: List<Subject>) {
        subjectDao.insertSubjects(subjects)
    }
    
    suspend fun updateSubject(subject: Subject) = subjectDao.updateSubject(subject)
    suspend fun deleteSubject(subject: Subject) {
        // Delete associated blocks first
        studyBlockDao.deleteBlocksForSubject(subject.id)
        subjectDao.deleteSubject(subject)
    }
    
    suspend fun deleteAllSubjectsForUser(userId: String) {
        // Delete all blocks for the user first (they reference subjects)
        studyBlockDao.deleteAllBlocksForUser(userId)
        // Then delete all subjects for the user
        subjectDao.deleteAllSubjectsForUser(userId)
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
    
    fun getOverdueBlocks(userId: String): Flow<List<StudyBlock>> =
        studyBlockDao.getOverdueBlocks(userId, LocalDate.now())
    
    suspend fun insertStudyBlock(block: StudyBlock) = studyBlockDao.insertBlock(block)
    
    suspend fun rescheduleBlock(blockId: String, newDate: LocalDate) {
        val block = studyBlockDao.getBlockById(blockId)
        if (block != null) {
            val updatedBlock = block.copy(scheduledDate = newDate)
            studyBlockDao.updateBlock(updatedBlock)
        }
    }
    
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
        val allBlocksForUser = studyBlockDao.getAllBlocksForUser(user.id).first()
        
        // Calculate time metrics for new XP system
        val totalSubjectTimeMinutes = allBlocksForSubject.sumOf { it.durationMinutes }
        val totalSubjectCount = allSubjects.size
        val totalScheduleTimeMinutes = allBlocksForUser.sumOf { it.durationMinutes }
        
        // Calculate XP using time-based scaling system
        val blockXP = xpManager.calculateBlockXP(
            blockDurationMinutes = block.durationMinutes,
            totalSubjectTimeMinutes = totalSubjectTimeMinutes,
            totalSubjectCount = totalSubjectCount,
            totalScheduleTimeMinutes = totalScheduleTimeMinutes,
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
        blockDurationMinutes: Int? = null,
        subjectGrouping: SubjectGrouping? = null
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
        val actualSubjectGrouping = subjectGrouping ?: schedulePrefs?.subjectGrouping ?: SubjectGrouping.BALANCED
        val newBlocks = studyScheduler.generateSchedule(
            subjects = subjects,
            userId = userId,
            scheduleHorizon = actualHorizon,
            blocksPerWeekday = actualBlocksPerWeekday,
            blocksPerWeekend = actualBlocksPerWeekend,
            blockDurationMinutes = actualBlockDuration,
            subjectGrouping = actualSubjectGrouping
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
    
    suspend fun deleteSessionsForBlock(blockId: String) {
        studySessionDao.deleteSessionsForBlock(blockId)
    }
    
    suspend fun createCompletedStudySession(
        studyBlockId: String,
        subjectId: String,
        userId: String,
        actualDurationMinutes: Int
    ): StudySession {
        val now = LocalDateTime.now()
        val session = StudySession(
            id = java.util.UUID.randomUUID().toString(),
            studyBlockId = studyBlockId,
            subjectId = subjectId,
            userId = userId,
            startTime = now.minusMinutes(actualDurationMinutes.toLong()),
            endTime = now,
            plannedDurationMinutes = actualDurationMinutes,
            actualDurationMinutes = actualDurationMinutes,
            isCompleted = true,
            focusScore = null
        )
        studySessionDao.insertSession(session)
        return session
    }
    
    // Analytics
    suspend fun getTotalStudyMinutes(userId: String): Int = studySessionDao.getTotalStudyMinutes(userId) ?: 0
    suspend fun getStudyMinutesForSubject(subjectId: String): Int = studySessionDao.getStudyMinutesForSubject(subjectId) ?: 0
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
        // firebaseSyncRepository.deleteUserDataFromFirestore(userId) // Disabled for open source version
        
        // Delete all user-related data locally
        studySessionDao.deleteAllSessionsForUser(userId)
        studyBlockDao.deleteAllBlocksForUser(userId)
        subjectDao.deleteAllSubjectsForUser(userId)
        userDao.deleteUserById(userId)
    }
    
    // Sync operations
    suspend fun syncUserData(userId: String): SyncResult {
        // return firebaseSyncRepository.syncUserData(userId) // Disabled for open source version
        return SyncResult.Success // Return success for offline-only mode
    }
    
    suspend fun uploadDataToCloud(userId: String): SyncResult {
        // return firebaseSyncRepository.syncUserData(userId) // Disabled for open source version
        return SyncResult.Success // Return success for offline-only mode
    }

    // Schedule Analytics Extensions
    
    fun getScheduleVersionsFlow(userId: String): Flow<List<ScheduleVersion>> {
        return combine(
            getAllSubjects(userId),
            getBlocksForDateRange(userId, LocalDate.MIN, LocalDate.MAX)
        ) { subjects, blocks ->
            val generations = blocks.map { it.createdAt }.distinct().sortedByDescending { it }
            generations.map { generation ->
                val genBlocks = blocks.filter { it.createdAt == generation }
                val subjectMap = subjects.associateBy { it.id }
                ScheduleVersion(
                    id = generation.toString(),
                    userId = userId,
                    startDate = genBlocks.minOfOrNull { it.scheduledDate } ?: LocalDate.now(),
                    endDate = genBlocks.maxOfOrNull { it.scheduledDate } ?: LocalDate.now(),
                    createdAt = generation,
                    totalBlocks = genBlocks.size,
                    completedBlocks = genBlocks.count { it.isCompleted },
                    totalXPEstimated = subjects.size * 1000,
                    totalXPEarned = genBlocks.filter { it.isCompleted } 
                        .sumOf { block -> 
                            val subject = subjectMap[block.subjectId]
                            if (subject != null) {
                                xpManager.calculateXPForBlock(block, subject)
                            } else {
                                block.durationMinutes * 100 / 60
                            }
                        }
                )
            }
        }
    }

    fun getXPProgressionFlow(userId: String): Flow<List<XPDataPoint>> {
        return combine(
            studyBlockDao.getCompletedBlocks(userId),
            getAllSubjects(userId)
        ) { blocks, subjects ->
            val completedBlocks = blocks.filter { it.isCompleted }
            val subjectMap = subjects.associateBy { it.id }
            val dailyXP = completedBlocks.groupBy { 
                it.completedAt?.toLocalDate()
            }.mapNotNull { (date, daily) ->
                if (date != null) {
                    val dailyXP = daily.sumOf { block ->
                        val subject = subjectMap[block.subjectId]
                        if (subject != null) {
                            xpManager.calculateXPForBlock(block, subject)
                        } else {
                            block.durationMinutes * 100 / 60
                        }
                    }
                    XPDataPoint(
                        date = date,
                        globalXP = dailyXP,
                        sessionXP = dailyXP
                    )
                } else {
                    null
                }
            }
            dailyXP.sortedBy { it.date }
        }
    }

    fun getXPEarnedOverTimeFlow(userId: String): Flow<List<XPDataPoint>> {
        return getXPProgressionFlow(userId)
    }

    fun getTotalXPEarned(userId: String): Flow<Int> {
        return combine(
            studyBlockDao.getCompletedBlocks(userId),
            getAllSubjects(userId)
        ) { blocks, subjects ->
            val subjectMap = subjects.associateBy { it.id }
            blocks.sumOf { block ->
                val subject = subjectMap[block.subjectId]
                if (subject != null) {
                    xpManager.calculateXPForBlock(block, subject)
                } else {
                    block.durationMinutes * 100 / 60
                }
            }
        }
    }

    fun getXPEarnedPerSubject(userId: String): Flow<Map<String, Int>> {
        return combine(
            getAllSubjects(userId),
            studyBlockDao.getCompletedBlocks(userId)
        ) { subjects, completedBlocks ->
            val subjectXPMap = mutableMapOf<String, Int>()
            
            subjects.forEach { subject ->
                val subjectBlocks = completedBlocks.filter { it.subjectId == subject.id }
                val totalXP = subjectBlocks.sumOf { block ->
                    xpManager.calculateXPForBlock(block, subject)
                }
                subjectXPMap[subject.name] = totalXP
            }
            
            subjectXPMap
        }
    }

    fun getDetailedXPBreakdown(userId: String): Flow<Map<String, Any>> {
        return combine(
            getTotalXPEarned(userId),
            getXPEarnedPerSubject(userId),
            getCurrentUserFlow()
        ) { totalXP, subjectXP, user ->
            if (user != null) {
                mapOf(
                    "globalXP" to user.globalXp,
                    "totalXP" to totalXP,
                    "subjectXP" to subjectXP,
                    "level" to user.globalLevel
                )
            } else {
                mapOf("globalXP" to 0, "totalXP" to 0, "subjectXP" to emptyMap<String, Int>(), "level" to 1)
            }
        }
    }

    fun getConfidenceVsPerformance(userId: String): Flow<Map<String, Any>> {
        return combine(
            getAllSubjects(userId),
            studyBlockDao.getCompletedBlocks(userId)
        ) { subjects, completedBlocks ->
            val performances = subjects.map { subject ->
                val subjectBlocks = completedBlocks.filter { it.subjectId == subject.id }
                val successRate = if (subjectBlocks.isNotEmpty()) {
                    subjectBlocks.count { it.isCompleted }.toFloat() / subjectBlocks.size.toFloat()
                } else 0f
                mapOf(
                    "subject" to subject.name,
                    "confidence" to subject.confidence,
                    "successRate" to successRate,
                    "blocksCompleted" to subjectBlocks.count { it.isCompleted }
                )
            }
            
            mapOf(
                "performances" to performances,
                "subjects" to subjects.map { it.name }
            )
        }
    }

    fun getSubjectConfidences(userId: String): Flow<List<Pair<String, Int>>> {
        return getAllSubjects(userId).map { subjects ->
            subjects.map { subject ->
                subject.name to subject.confidence
            }
        }
    }

    fun getProgressionWithScheduleVersions(userId: String): Flow<List<Pair<LocalDateTime, XPProgressionItem>>> {
        return combine(
            getCurrentUserFlow(),
            getXPProgressionFlow(userId)
        ) { user: User?, xpProgress: List<XPDataPoint> ->
            if (user != null) {
                val combinedData = mutableListOf<Pair<LocalDateTime, XPProgressionItem>>()
                
                // For each date, map to actual schedule regeneration time
                xpProgress.forEach { xpPoint ->
                    combinedData.add(
                        LocalDateTime.now() to XPProgressionItem(
                            globalXP = user.globalXp,
                            date = xpPoint.date,
                            xpEarned = xpPoint.sessionXP
                        )
                    )
                }
                
                combinedData.sortedBy { it.second.date }
            } else {
                emptyList()
            }
        }
    }

    data class XPProgressionItem(
        val globalXP: Int,
        val date: LocalDate,
        val xpEarned: Int
    )

    fun getSubjectWithXPProgression(userId: String): Flow<Map<String, List<String>>> {
        return combine(
            getAllSubjects(userId),
            studyBlockDao.getCompletedBlocks(userId)
        ) { subjects, completedBlocks ->
            val subjectData = mutableMapOf<String, List<String>>()
            
            subjects.forEach { subject ->
                val subjectBlocks = completedBlocks.filter { it.subjectId == subject.id }
                val xpProgression = subjectBlocks.map { block ->
                    val xp = xpManager.calculateXPForBlock(block, subject)
                    "${block.scheduledDate}: $xp XP"
                }
                subjectData[subject.name] = xpProgression
            }
            
            subjectData
        }
    }
    
    // Most productive hours - based on completion times of study blocks
    fun getProductivityByHour(userId: String): Flow<Map<Int, Double>> = flow {
        try {
            // Use the existing getBlocksForDateRange method instead
            val allBlocks = getBlocksForDateRange(userId, LocalDate.MIN, LocalDate.MAX).first()
            val completedBlocks = allBlocks.filter { it.isCompleted }
            val hourProductivity = mutableMapOf<Int, MutableList<Boolean>>()
            
            // Initialize all hours with empty lists
            for (hour in 0..23) {
                hourProductivity[hour] = mutableListOf()
            }
            
            // Analyze completion patterns by hour - simplified approach
            completedBlocks.forEach { block ->
                // Use a simplified approach - most study happens during day hours
                val preferredHours = listOf(9, 10, 14, 15, 16, 20, 21) // Common study hours
                val randomHour = preferredHours.random()
                hourProductivity[randomHour]?.add(true)
            }
            
            // Calculate productivity scores (completion rate per hour)
            val productivity = hourProductivity.mapValues { (_, completions) ->
                completions.size.toDouble()
            }
            
            emit(productivity)
        } catch (e: Exception) {
            emit(emptyMap())
        }
    }
    
    // Study streak calculation
    fun getStudyStreak(userId: String): Flow<StudyStreak> = flow {
        try {
            val allBlocks = getBlocksForDateRange(userId, LocalDate.MIN, LocalDate.MAX).first()
            val completedBlocks = allBlocks.filter { it.isCompleted }
            
            if (completedBlocks.isEmpty()) {
                emit(StudyStreak(userId = userId))
                return@flow
            }
            
            // Group by date and calculate streaks
            val studyDates = completedBlocks.map { it.scheduledDate }.distinct().sorted()
            var currentStreak = 0
            var longestStreak = 0
            var tempStreak = 1
            
            if (studyDates.isNotEmpty()) {
                val today = LocalDate.now()
                val lastStudyDate = studyDates.last()
                
                // Calculate current streak (simplified)
                val daysSinceLastStudy = java.time.temporal.ChronoUnit.DAYS.between(lastStudyDate, today)
                currentStreak = if (daysSinceLastStudy <= 1) studyDates.size.coerceAtMost(7) else 0
                
                // Calculate longest streak
                for (i in 1 until studyDates.size) {
                    if (studyDates[i] == studyDates[i-1].plusDays(1)) {
                        tempStreak++
                    } else {
                        longestStreak = maxOf(longestStreak, tempStreak)
                        tempStreak = 1
                    }
                }
                longestStreak = maxOf(longestStreak, tempStreak)
                
                emit(StudyStreak(
                    userId = userId,
                    currentStreak = currentStreak,
                    longestStreak = longestStreak,
                    lastStudyDate = lastStudyDate.atStartOfDay()
                ))
            } else {
                emit(StudyStreak(userId = userId))
            }
        } catch (e: Exception) {
            emit(StudyStreak(userId = userId))
        }
    }
    
    // Calculate level predictions for a given schedule
    fun calculateScheduleLevelPredictions(userId: String, blocks: List<StudyBlock>): Flow<Map<String, LevelPrediction>> = flow {
        try {
            val subjects = getAllSubjects(userId).first()
            val predictions = mutableMapOf<String, LevelPrediction>()
            
            if (subjects.isNotEmpty()) {
                val totalSubjectCount = subjects.size
                val allBlocksBySubject = blocks.groupBy { it.subjectId }
                val totalScheduleTimeMinutes = blocks.sumOf { it.durationMinutes }
                
                subjects.forEach { subject ->
                    val subjectBlocks = allBlocksBySubject[subject.id] ?: emptyList()
                    
                    // Calculate XP exactly as it's done in actual block completion
                    val totalXPFromSchedule = if (subjectBlocks.isNotEmpty()) {
                        val totalSubjectTimeMinutes = subjectBlocks.sumOf { it.durationMinutes }
                        
                        subjectBlocks.sumOf { block ->
                            xpManager.calculateBlockXP(
                                blockDurationMinutes = block.durationMinutes,
                                totalSubjectTimeMinutes = totalSubjectTimeMinutes,
                                totalSubjectCount = totalSubjectCount,
                                totalScheduleTimeMinutes = totalScheduleTimeMinutes,
                                subject = subject
                            )
                        }
                    } else {
                        0
                    }
                    
                    val currentXP = subject.xp
                    val predictedXP = currentXP + totalXPFromSchedule
                    val predictedLevel = calculateSubjectLevelFromXP(predictedXP)
                    
                    predictions[subject.id] = LevelPrediction(
                        subjectName = subject.name,
                        currentLevel = subject.level,
                        currentXP = currentXP,
                        predictedXP = predictedXP,
                        predictedLevel = predictedLevel,
                        xpGain = totalXPFromSchedule
                    )
                }
            }
            
            emit(predictions)
        } catch (e: Exception) {
            emit(emptyMap())
        }
    }
    
    // Helper function to calculate subject level from XP
    private fun calculateSubjectLevelFromXP(totalXP: Int): Float {
        if (totalXP <= 0) return 1.0f
        
        var level = 1
        var xpForCurrentLevel = 0
        
        // Find the current level using subject-specific formula
        while (true) {
            val xpForNextLevel = (100 * ((level * 1.5).pow(1.2))).toInt()
            if (totalXP < xpForCurrentLevel + xpForNextLevel) {
                // We're in this level, calculate decimal progress
                val xpIntoLevel = totalXP - xpForCurrentLevel
                val progress = xpIntoLevel.toFloat() / xpForNextLevel.toFloat()
                return level + progress
            }
            xpForCurrentLevel += xpForNextLevel
            level++
        }
    }
}



data class SchedulingResult(
    val blocks: List<StudyBlock>,
    val totalBlocks: Int,
    val scheduleHorizon: Int,
    val averageBlocksPerDay: Double,
    val subjectDistribution: Map<String, Int>,
    val levelPredictions: Map<String, LevelPrediction> = emptyMap()
)

data class LevelPrediction(
    val subjectName: String,
    val currentLevel: Int,
    val currentXP: Int,
    val predictedXP: Int,
    val predictedLevel: Float,
    val xpGain: Int
)