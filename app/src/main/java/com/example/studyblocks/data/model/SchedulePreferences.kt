package com.example.studyblocks.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SubjectGrouping(val displayName: String, val description: String) {
    MOST_GROUPED("Most Grouped", "Study the same subject for multiple blocks in a row"),
    BALANCED("Balanced", "Mix subjects evenly throughout each day"),
    LEAST_GROUPED("Least Grouped", "Maximize subject variety with minimal repetition")
}

@Entity(tableName = "schedule_preferences")
data class SchedulePreferences(
    @PrimaryKey val userId: String,
    val scheduleHorizonDays: Int = 21, // How many days ahead to plan
    val blocksPerWeekday: Int = 3, // Study blocks for Monday-Friday
    val blocksPerWeekend: Int = 2, // Study blocks for Saturday-Sunday
    val defaultBlockDurationMinutes: Int = 60, // Length of each study session
    val subjectGrouping: SubjectGrouping = SubjectGrouping.BALANCED, // How to group subjects
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),
    val updatedAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
) {
    init {
        require(scheduleHorizonDays in 7..90) { "Schedule horizon must be between 7 and 90 days" }
        require(blocksPerWeekday in 1..8) { "Blocks per weekday must be between 1 and 8" }
        require(blocksPerWeekend in 0..6) { "Blocks per weekend must be between 0 and 6" }
        require(defaultBlockDurationMinutes in 15..180) { "Block duration must be between 15 and 180 minutes" }
    }
    
    // Calculate total daily study time for weekdays
    val totalWeekdayStudyTimeMinutes: Int
        get() = blocksPerWeekday * defaultBlockDurationMinutes
    
    // Calculate total daily study time for weekends
    val totalWeekendStudyTimeMinutes: Int
        get() = blocksPerWeekend * defaultBlockDurationMinutes
    
    // Format study time as hours and minutes string
    fun formatStudyTime(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours == 0 -> "${mins}m"
            mins == 0 -> "${hours}h"
            else -> "${hours}h ${mins}m"
        }
    }
    
    val weekdayStudyTimeFormatted: String
        get() = formatStudyTime(totalWeekdayStudyTimeMinutes)
    
    val weekendStudyTimeFormatted: String
        get() = formatStudyTime(totalWeekendStudyTimeMinutes)
    
    // Convert days to weeks for UI display
    val scheduleHorizonWeeks: Int
        get() = (scheduleHorizonDays + 6) / 7 // Round up to nearest week
    
    // Helper to create SchedulePreferences from weeks
    companion object {
        fun fromWeeks(
            userId: String,
            scheduleHorizonWeeks: Int,
            blocksPerWeekday: Int,
            blocksPerWeekend: Int,
            defaultBlockDurationMinutes: Int,
            subjectGrouping: SubjectGrouping = SubjectGrouping.BALANCED
        ): SchedulePreferences {
            return SchedulePreferences(
                userId = userId,
                scheduleHorizonDays = scheduleHorizonWeeks * 7,
                blocksPerWeekday = blocksPerWeekday,
                blocksPerWeekend = blocksPerWeekend,
                defaultBlockDurationMinutes = defaultBlockDurationMinutes,
                subjectGrouping = subjectGrouping
            )
        }
    }
}

// Helper data class for onboarding
data class OnboardingSchedulePreferences(
    val scheduleHorizonDays: Int = 21,
    val blocksPerWeekday: Int = 3,
    val blocksPerWeekend: Int = 2,
    val defaultBlockDurationMinutes: Int = 60,
    val subjectGrouping: SubjectGrouping = SubjectGrouping.BALANCED
) {
    fun toSchedulePreferences(userId: String): SchedulePreferences {
        return SchedulePreferences(
            userId = userId,
            scheduleHorizonDays = scheduleHorizonDays,
            blocksPerWeekday = blocksPerWeekday,
            blocksPerWeekend = blocksPerWeekend,
            defaultBlockDurationMinutes = defaultBlockDurationMinutes,
            subjectGrouping = subjectGrouping
        )
    }
}