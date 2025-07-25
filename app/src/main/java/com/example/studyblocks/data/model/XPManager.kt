package com.example.studyblocks.data.model

import kotlin.math.max
import kotlin.math.pow

object XPManager {
    
    // Base XP for entire schedule - 100% completion of all blocks gives this amount
    private const val TOTAL_SCHEDULE_XP = 1000
    
    /**
     * Calculate XP for completing a study block
     * XP is distributed equally across all subjects, then proportionally within each subject
     * Each subject gets equal total XP regardless of time allocation
     */
    fun calculateBlockXP(
        blockDurationMinutes: Int,
        totalSubjectTimeMinutes: Int,
        totalSubjectCount: Int,
        subject: Subject
    ): Int {
        if (totalSubjectTimeMinutes <= 0 || totalSubjectCount <= 0) return 0
        
        // Each subject gets equal share of total XP
        val xpPerSubject = TOTAL_SCHEDULE_XP.toDouble() / totalSubjectCount
        
        // Within each subject, XP is distributed proportionally by time
        val subjectXpRate = xpPerSubject / totalSubjectTimeMinutes
        
        // XP for this block based on its duration within the subject
        val blockXp = blockDurationMinutes * subjectXpRate
        
        return blockXp.toInt()
    }
    
    /**
     * Calculate XP for custom blocks based on time invested
     * Uses a consistent rate of 100 XP per hour
     */
    fun calculateCustomBlockXP(
        allSubjects: List<Subject>,
        customBlockDurationMinutes: Int
    ): Int {
        // Base XP for custom block: 100 XP per hour
        val customXP = (customBlockDurationMinutes / 60.0 * 100)
        
        return customXP.toInt()
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