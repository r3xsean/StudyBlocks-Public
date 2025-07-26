package com.example.studyblocks.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import kotlin.math.pow

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val confidence: Int, // 1-10 scale
    val blockDurationMinutes: Int,
    val xp: Int = 0,
    val level: Int = 1,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val userId: String
) {
    init {
        require(confidence in 1..10) { "Confidence must be between 1 and 10" }
        require(blockDurationMinutes in 15..120) { "Block duration must be between 15 and 120 minutes" }
        require(name.isNotBlank()) { "Subject name cannot be blank" }
        require(xp >= 0) { "XP cannot be negative" }
        require(level >= 1) { "Level must be at least 1" }
    }
    
    val confidenceWeight: Double
        get() = when (confidence) {
            1 -> 4.5   // Very low confidence gets 4.5x weight
            2 -> 3.7   // Low confidence gets 3.7x weight 
            3 -> 2.8   // Still struggling gets 2.8x weight
            4 -> 2.3   // Below average gets 2.3x weight
            5 -> 1.9   // Average gets 1.9x weight
            6 -> 1.4   // Slightly above average gets 1.4x weight
            7 -> 1.0   // Good confidence gets normal weight
            8 -> 0.8   // Very good gets reduced weight
            9 -> 0.6   // Excellent gets 0.6x weight
            10 -> 0.4  // Perfect confidence gets 0.4x weight
            else -> 1.0
        }
    
    // Calculate XP required for next level (exponential growth)
    val xpForNextLevel: Int
        get() = (100 * (level * 1.5).pow(1.2)).toInt()
    
    // Calculate XP required for current level
    val xpForCurrentLevel: Int
        get() = if (level == 1) 0 else (100 * ((level - 1) * 1.5).pow(1.2)).toInt()
    
    // Progress towards next level (0.0 to 1.0)
    val levelProgress: Float
        get() {
            val currentLevelXp = xpForCurrentLevel
            val nextLevelXp = xpForNextLevel
            val progressXp = xp - currentLevelXp
            val totalNeeded = nextLevelXp - currentLevelXp
            return if (totalNeeded > 0) (progressXp.toFloat() / totalNeeded).coerceIn(0f, 1f) else 1f
        }
}