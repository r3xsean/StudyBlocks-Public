package com.example.studyblocks.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import kotlin.math.pow

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String,
    val profilePictureUrl: String? = null,
    val defaultBlockDuration: Int = 30, // minutes
    val preferredBlocksPerDay: Int = 3, // preferred number of blocks per day
    val globalXp: Int = 0,
    val globalLevel: Int = 1,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastSyncAt: LocalDateTime = LocalDateTime.now(),
    val isSignedIn: Boolean = false
) {
    init {
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(displayName.isNotBlank()) { "Display name cannot be blank" }
        require(defaultBlockDuration in 15..120) { "Default block duration must be between 15 and 120 minutes" }
        require(preferredBlocksPerDay in 1..8) { "Preferred blocks per day must be between 1 and 8" }
        require(globalXp >= 0) { "Global XP cannot be negative" }
        require(globalLevel >= 1) { "Global level must be at least 1" }
    }
    
    // Calculate XP required for next global level (exponential growth, higher than subject levels)
    val xpForNextGlobalLevel: Int
        get() = (200 * (globalLevel * 1.8).pow(1.3)).toInt()
    
    // Calculate XP required for current global level
    val xpForCurrentGlobalLevel: Int
        get() = if (globalLevel == 1) 0 else (200 * ((globalLevel - 1) * 1.8).pow(1.3)).toInt()
    
    // Progress towards next global level (0.0 to 1.0)
    val globalLevelProgress: Float
        get() {
            val currentLevelXp = xpForCurrentGlobalLevel
            val nextLevelXp = xpForNextGlobalLevel
            val progressXp = globalXp - currentLevelXp
            val totalNeeded = nextLevelXp - currentLevelXp
            return if (totalNeeded > 0) (progressXp.toFloat() / totalNeeded).coerceIn(0f, 1f) else 1f
        }
}

data class UserPreferences(
    val userId: String,
    val theme: AppTheme = AppTheme.AUTO,
    val notificationsEnabled: Boolean = true,
    val studyRemindersEnabled: Boolean = true,
    val breakRemindersEnabled: Boolean = true,
    val focusModeEnabled: Boolean = false,
    val autoStartTimer: Boolean = false,
    val language: String = "en",
    val morningRemindersEnabled: Boolean = true,
    val afternoonRemindersEnabled: Boolean = true,
    val eveningRemindersEnabled: Boolean = true
)

enum class AppTheme {
    LIGHT,
    DARK,
    AUTO
}