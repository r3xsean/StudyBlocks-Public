package com.example.studyblocks.data.local.dao

import androidx.room.*
import com.example.studyblocks.data.model.StudySession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface StudySessionDao {
    
    @Query("SELECT * FROM study_sessions WHERE userId = :userId ORDER BY startTime DESC")
    fun getAllSessions(userId: String): Flow<List<StudySession>>
    
    @Query("SELECT * FROM study_sessions WHERE id = :id")
    suspend fun getSessionById(id: String): StudySession?
    
    @Query("SELECT * FROM study_sessions WHERE studyBlockId = :blockId")
    fun getSessionsForBlock(blockId: String): Flow<List<StudySession>>
    
    @Query("SELECT * FROM study_sessions WHERE subjectId = :subjectId ORDER BY startTime DESC")
    fun getSessionsForSubject(subjectId: String): Flow<List<StudySession>>
    
    @Query("SELECT * FROM study_sessions WHERE userId = :userId AND endTime IS NULL AND isCompleted = 0 LIMIT 1")
    suspend fun getActiveSession(userId: String): StudySession?
    
    @Query("SELECT * FROM study_sessions WHERE userId = :userId AND endTime IS NULL AND isCompleted = 0 LIMIT 1")
    fun getActiveSessionFlow(userId: String): Flow<StudySession?>
    
    @Query("SELECT * FROM study_sessions WHERE userId = :userId AND startTime BETWEEN :startTime AND :endTime")
    fun getSessionsInTimeRange(userId: String, startTime: LocalDateTime, endTime: LocalDateTime): Flow<List<StudySession>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession)
    
    @Update
    suspend fun updateSession(session: StudySession)
    
    @Delete
    suspend fun deleteSession(session: StudySession)
    
    @Query("DELETE FROM study_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: String)
    
    @Query("DELETE FROM study_sessions WHERE studyBlockId = :blockId")
    suspend fun deleteSessionsForBlock(blockId: String)
    
    @Query("DELETE FROM study_sessions WHERE userId = :userId")
    suspend fun deleteAllSessionsForUser(userId: String)
    
    @Query("SELECT SUM(actualDurationMinutes) FROM study_sessions WHERE userId = :userId AND isCompleted = 1")
    suspend fun getTotalStudyMinutes(userId: String): Int?
    
    @Query("SELECT COUNT(*) FROM study_sessions WHERE userId = :userId AND isCompleted = 1")
    suspend fun getTotalCompletedSessions(userId: String): Int
    
    @Query("SELECT AVG(actualDurationMinutes) FROM study_sessions WHERE userId = :userId AND isCompleted = 1")
    suspend fun getAverageSessionDuration(userId: String): Double?
    
    @Query("SELECT SUM(actualDurationMinutes) FROM study_sessions WHERE subjectId = :subjectId AND isCompleted = 1")
    suspend fun getStudyMinutesForSubject(subjectId: String): Int?
}