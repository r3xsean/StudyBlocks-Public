package com.example.studyblocks.data.local.dao

import androidx.room.*
import com.example.studyblocks.data.model.SchedulePreferences
import kotlinx.coroutines.flow.Flow

@Dao
interface SchedulePreferencesDao {
    
    @Query("SELECT * FROM schedule_preferences WHERE userId = :userId")
    suspend fun getSchedulePreferences(userId: String): SchedulePreferences?
    
    @Query("SELECT * FROM schedule_preferences WHERE userId = :userId")
    fun getSchedulePreferencesFlow(userId: String): Flow<SchedulePreferences?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedulePreferences(preferences: SchedulePreferences)
    
    @Update
    suspend fun updateSchedulePreferences(preferences: SchedulePreferences)
    
    @Delete
    suspend fun deleteSchedulePreferences(preferences: SchedulePreferences)
    
    @Query("DELETE FROM schedule_preferences WHERE userId = :userId")
    suspend fun deletePreferencesForUser(userId: String)
}