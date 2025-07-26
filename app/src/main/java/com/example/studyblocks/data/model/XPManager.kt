package com.example.studyblocks.data.model

import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

object XPManager {
    
    // Base XP rate: 100 XP per hour of study time
    private const val BASE_XP_PER_HOUR = 100
    
    /**
     * Calculate XP for completing a study block with time-based scaling
     * XP scales with total schedule time and is distributed equally across all subjects
     * Each subject gets equal total XP regardless of confidence or time allocation
     */
    fun calculateBlockXP(
        blockDurationMinutes: Int,
        totalSubjectTimeMinutes: Int,
        totalSubjectCount: Int,
        totalScheduleTimeMinutes: Int,
        subject: Subject
    ): Int {
        if (totalSubjectTimeMinutes <= 0 || totalSubjectCount <= 0 || totalScheduleTimeMinutes <= 0) return 0
        
        // Calculate total XP for entire schedule based on time (100 XP per hour)
        val totalScheduleXP = (totalScheduleTimeMinutes / 60.0 * BASE_XP_PER_HOUR)
        
        // Each subject gets equal share of total XP
        val xpPerSubject = totalScheduleXP / totalSubjectCount
        
        // Within each subject, XP is distributed proportionally by time
        val subjectXpRate = xpPerSubject / totalSubjectTimeMinutes
        
        // XP for this block based on its duration within the subject
        val blockXp = blockDurationMinutes * subjectXpRate
        
        // Use proper rounding to minimize XP loss
        return blockXp.roundToInt()
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
        val customXP = (customBlockDurationMinutes / 60.0 * BASE_XP_PER_HOUR)
        
        return customXP.roundToInt()
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
        return if (level == 1) 0 else (100 * ((level - 1) * 1.5).pow(1.2)).roundToInt()
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
        return if (level == 1) 0 else (200 * ((level - 1) * 1.8).pow(1.3)).roundToInt()
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
     * Calculate XP for completing a study block (simplified approach)
     * Uses consistent time-based XP for all blocks: 100 XP per hour
     * @deprecated Use calculateBlockXP with proper schedule context instead
     */
    fun calculateXPForBlock(
        block: StudyBlock,
        subject: Subject
    ): Int {
        // Use consistent time-based XP calculation for all blocks
        return (block.durationMinutes / 60.0 * BASE_XP_PER_HOUR).roundToInt()
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