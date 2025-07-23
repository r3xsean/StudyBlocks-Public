package com.example.studyblocks.data.local.dao

import androidx.room.*
import com.example.studyblocks.data.model.Subject
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    
    @Query("SELECT * FROM subjects WHERE userId = :userId ORDER BY name ASC")
    fun getAllSubjects(userId: String): Flow<List<Subject>>
    
    @Query("SELECT * FROM subjects WHERE userId = :userId ORDER BY confidence ASC")
    fun getSubjectsByConfidence(userId: String): Flow<List<Subject>>
    
    @Query("SELECT * FROM subjects WHERE userId = :userId ORDER BY level DESC")
    fun getSubjectsByLevel(userId: String): Flow<List<Subject>>
    
    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: String): Subject?
    
    @Query("SELECT * FROM subjects WHERE id = :id")
    fun getSubjectByIdFlow(id: String): Flow<Subject?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<Subject>)
    
    @Update
    suspend fun updateSubject(subject: Subject)
    
    @Delete
    suspend fun deleteSubject(subject: Subject)
    
    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubjectById(id: String)
    
    @Query("DELETE FROM subjects WHERE userId = :userId")
    suspend fun deleteAllSubjectsForUser(userId: String)
    
    @Query("SELECT COUNT(*) FROM subjects WHERE userId = :userId")
    suspend fun getSubjectCount(userId: String): Int
    
    @Query("UPDATE subjects SET confidence = :confidence, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSubjectConfidence(id: String, confidence: Int, updatedAt: java.time.LocalDateTime)
    
    @Query("UPDATE subjects SET xp = :xp, level = :level, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSubjectXP(id: String, xp: Int, level: Int, updatedAt: java.time.LocalDateTime)
}