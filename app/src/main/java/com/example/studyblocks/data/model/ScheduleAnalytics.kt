package com.example.studyblocks.data.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Represents a schedule generation with version tracking
 */
data class ScheduleVersion(
    val id: String,
    val userId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val regeneratedAt: LocalDateTime? = null,
    val isActive: Boolean = true,
    val totalBlocks: Int,
    val completedBlocks: Int = 0,
    val totalXPEstimated: Int,
    val totalXPEarned: Int = 0,
    val notes: String? = null
)

/**
 * XP data point for tracking XP over time
 */
data class XPDataPoint(
    val date: LocalDate,
    val globalXP: Int,
    val subjectXP: Map<String, Int> = emptyMap(),
    val sessionXP: Int = 0,
    val scheduleVersionId: String? = null
)

/**
 * Analytics for a specific schedule
 */
data class ScheduleAnalytics(
    val version: ScheduleVersion,
    val completionRate: Float,
    val avgSessionDuration: Double,
    val totalStudyTimeMinutes: Int,
    val subjectsProgress: List<SubjectScheduleProgress>,
    val confidenceChanges: List<ConfidenceChange>,
    val xpProgression: List<XPDataPoint>,
    val efficiencyScore: Float
)

/**
 * Subject progress within a specific schedule
 */
data class SubjectScheduleProgress(
    val subjectId: String,
    val subjectName: String,
    val totalBlocks: Int,
    val completedBlocks: Int,
    val initialConfidence: Int,
    val finalConfidence: Int,
    val totalMinutes: Int,
    val xpEarned: Int,
    val performanceScore: Float,
    val improvementScore: Float
)

/**
 * Track confidence changes between schedule generations
 */
data class ConfidenceChange(
    val subjectId: String,
    val subjectName: String,
    val oldConfidence: Int,
    val newConfidence: Int,
    val changePercent: Int,
    val affectedByScheduleVersion: String
)

/**
 * Learning insights generated from schedule analytics
 */
data class LearningInsights(
    val bestPerformingSubject: String,
    val mostImprovedSubject: String,
    val consistencyScore: Float,
    val scheduleEfficiency: Float,
    val recommendations: List<String>
)

/**
 * Analytics period based on schedule versions
 */
sealed class AnalyticsPeriod {
    data object CurrentSchedule : AnalyticsPeriod()
    data object PreviousSchedule : AnalyticsPeriod()
    data object AllSchedules : AnalyticsPeriod()
    data class CustomScheduleRange(val startId: String, val endId: String) : AnalyticsPeriod()
}