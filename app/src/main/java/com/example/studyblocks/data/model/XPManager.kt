package com.example.studyblocks.data.model

import kotlin.math.max
import kotlin.math.pow

object XPManager {
    
    // Base XP per subject - 100% completion of all blocks in schedule gives this amount
    private const val BASE_XP_PER_SUBJECT = 1000
    
    /**
     * Calculate XP for completing a study block
     * XP is based on the percentage of total blocks completed for that subject
     * 100% completion of all subject blocks = BASE_XP_PER_SUBJECT regardless of total count
     */
    fun calculateBlockXP(
        completedBlocks: Int,
        totalBlocksInSchedule: Int,
        subject: Subject
    ): Int {
        if (totalBlocksInSchedule <= 0) return 0
        
        // Base XP per block for this subject
        val baseXpPerBlock = BASE_XP_PER_SUBJECT / totalBlocksInSchedule
        
        // Confidence multiplier - lower confidence gives slightly more XP to encourage practice
        val confidenceMultiplier = when (subject.confidence) {
            in 1..3 -> 1.2f  // Struggling subjects get 20% bonus
            in 4..6 -> 1.1f  // Learning subjects get 10% bonus
            in 7..8 -> 1.0f  // Good confidence gets normal XP
            else -> 0.9f     // Excellent confidence gets 10% less (already mastered)
        }
        
        return (baseXpPerBlock * confidenceMultiplier).toInt()
    }
    
    /**
     * Calculate XP for custom blocks based on average confidence of all subjects
     */
    fun calculateCustomBlockXP(
        allSubjects: List<Subject>,
        customBlockDurationMinutes: Int
    ): Int {
        if (allSubjects.isEmpty()) return 50 // Default XP if no subjects
        
        // Average confidence across all subjects
        val averageConfidence = allSubjects.map { it.confidence }.average()
        
        // Base XP for custom block (scaled by duration)
        val baseCustomXP = (customBlockDurationMinutes / 60.0 * 100).toInt()
        
        // Confidence multiplier based on average confidence
        val confidenceMultiplier = when (averageConfidence.toInt()) {
            in 1..3 -> 1.2f
            in 4..6 -> 1.1f
            in 7..8 -> 1.0f
            else -> 0.9f
        }
        
        return (baseCustomXP * confidenceMultiplier).toInt()
    }
    
    /**
     * Calculate new level based on XP
     */
    fun calculateLevel(xp: Int): Int {
        var level = 1
        while (xp >= getXpForLevel(level + 1)) {
            level++
        }
        return level
    }
    
    /**
     * Calculate XP required for a specific level (for subjects)
     */
    fun getXpForLevel(level: Int): Int {
        return if (level == 1) 0 else (100 * ((level - 1) * 1.5).pow(1.2)).toInt()
    }
    
    /**
     * Calculate global XP based on all subject XP
     */
    fun calculateGlobalXP(subjects: List<Subject>): Int {
        return subjects.sumOf { it.xp }
    }
    
    /**
     * Calculate global level based on global XP
     */
    fun calculateGlobalLevel(globalXp: Int): Int {
        var level = 1
        while (globalXp >= getGlobalXpForLevel(level + 1)) {
            level++
        }
        return level
    }
    
    /**
     * Calculate XP required for a specific global level
     */
    fun getGlobalXpForLevel(level: Int): Int {
        return if (level == 1) 0 else (200 * ((level - 1) * 1.8).pow(1.3)).toInt()
    }
    
    /**
     * Update subject with new XP and level
     */
    fun updateSubjectXP(subject: Subject, xpChange: Int): Subject {
        val newXp = max(0, subject.xp + xpChange)
        val newLevel = calculateLevel(newXp)
        
        return subject.copy(
            xp = newXp,
            level = newLevel,
            updatedAt = java.time.LocalDateTime.now()
        )
    }
    
    /**
     * Update user with new global XP and level based on subjects
     */
    fun updateUserGlobalXP(user: User, subjects: List<Subject>): User {
        val newGlobalXp = calculateGlobalXP(subjects)
        val newGlobalLevel = calculateGlobalLevel(newGlobalXp)
        
        return user.copy(
            globalXp = newGlobalXp,
            globalLevel = newGlobalLevel,
            lastSyncAt = java.time.LocalDateTime.now()
        )
    }
}