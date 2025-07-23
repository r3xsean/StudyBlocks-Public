package com.example.studyblocks.data.local.dao

import androidx.room.*
import com.example.studyblocks.data.model.StudyBlock
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface StudyBlockDao {
    
    @Query("SELECT * FROM study_blocks WHERE userId = :userId AND scheduledDate = :date ORDER BY blockNumber ASC")
    fun getBlocksForDate(userId: String, date: LocalDate): Flow<List<StudyBlock>>
    
    @Query("SELECT * FROM study_blocks WHERE userId = :userId AND scheduledDate BETWEEN :startDate AND :endDate ORDER BY scheduledDate ASC, blockNumber ASC")
    fun getBlocksForDateRange(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<StudyBlock>>
    
    @Query("SELECT * FROM study_blocks WHERE subjectId = :subjectId ORDER BY scheduledDate ASC")
    fun getBlocksForSubject(subjectId: String): Flow<List<StudyBlock>>
    
    @Query("SELECT * FROM study_blocks WHERE id = :id")
    suspend fun getBlockById(id: String): StudyBlock?
    
    @Query("SELECT * FROM study_blocks WHERE userId = :userId AND isCompleted = 0 AND scheduledDate < :currentDate")
    fun getOverdueBlocks(userId: String, currentDate: LocalDate): Flow<List<StudyBlock>>
    
    @Query("SELECT * FROM study_blocks WHERE userId = :userId AND isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedBlocks(userId: String): Flow<List<StudyBlock>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: StudyBlock)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlocks(blocks: List<StudyBlock>)
    
    @Update
    suspend fun updateBlock(block: StudyBlock)
    
    @Delete
    suspend fun deleteBlock(block: StudyBlock)
    
    @Query("DELETE FROM study_blocks WHERE id = :id")
    suspend fun deleteBlockById(id: String)
    
    @Query("DELETE FROM study_blocks WHERE subjectId = :subjectId")
    suspend fun deleteBlocksForSubject(subjectId: String)
    
    @Query("DELETE FROM study_blocks WHERE userId = :userId")
    suspend fun deleteAllBlocksForUser(userId: String)
    
    @Query("DELETE FROM study_blocks WHERE userId = :userId AND isCompleted = 0")
    suspend fun deletePendingBlocksForUser(userId: String)
    
    @Query("UPDATE study_blocks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun markBlockComplete(id: String, isCompleted: Boolean, completedAt: java.time.LocalDateTime?)
    
    @Query("SELECT COUNT(*) FROM study_blocks WHERE userId = :userId AND isCompleted = 1")
    suspend fun getTotalCompletedBlocks(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM study_blocks WHERE userId = :userId AND scheduledDate = :date AND isCompleted = 1")
    suspend fun getCompletedBlocksForDate(userId: String, date: LocalDate): Int
}