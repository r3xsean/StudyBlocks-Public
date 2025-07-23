package com.example.studyblocks.data.local.dao

import androidx.room.*
import com.example.studyblocks.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): User?
    
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserByIdFlow(id: String): Flow<User?>
    
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?
    
    @Query("SELECT * FROM users WHERE isSignedIn = 1 LIMIT 1")
    suspend fun getCurrentUser(): User?
    
    @Query("SELECT * FROM users WHERE isSignedIn = 1 LIMIT 1")
    fun getCurrentUserFlow(): Flow<User?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Update
    suspend fun updateUser(user: User)
    
    @Delete
    suspend fun deleteUser(user: User)
    
    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: String)
    
    @Query("UPDATE users SET isSignedIn = 0")
    suspend fun signOutAllUsers()
    
    @Query("UPDATE users SET isSignedIn = 1 WHERE id = :userId")
    suspend fun signInUser(userId: String)
    
    @Query("UPDATE users SET lastSyncAt = :syncTime WHERE id = :userId")
    suspend fun updateLastSync(userId: String, syncTime: java.time.LocalDateTime)
    
    @Query("UPDATE users SET globalXp = :globalXp, globalLevel = :globalLevel, lastSyncAt = :syncTime WHERE id = :userId")
    suspend fun updateUserGlobalXP(userId: String, globalXp: Int, globalLevel: Int, syncTime: java.time.LocalDateTime)
}