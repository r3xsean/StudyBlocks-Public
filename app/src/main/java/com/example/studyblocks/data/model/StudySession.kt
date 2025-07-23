package com.example.studyblocks.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey val id: String,
    val studyBlockId: String,
    val subjectId: String,
    val userId: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val plannedDurationMinutes: Int,
    val actualDurationMinutes: Int? = null,
    val isCompleted: Boolean = false,
    val breaksTaken: Int = 0,
    val focusScore: Int? = null // 1-10 rating of focus during session
) {
    val duration: Long?
        get() = if (endTime != null) {
            java.time.temporal.ChronoUnit.MINUTES.between(startTime, endTime)
        } else null
    
    val isActive: Boolean
        get() = endTime == null && !isCompleted
}

data class StudyStreak(
    val userId: String,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastStudyDate: LocalDateTime? = null
)

data class StudyStats(
    val userId: String,
    val totalBlocksCompleted: Int = 0,
    val totalMinutesStudied: Int = 0,
    val averageSessionDuration: Double = 0.0,
    val mostProductiveHour: Int? = null,
    val weeklyGoalMinutes: Int = 0,
    val currentWeekMinutes: Int = 0
)