package com.example.studyblocks.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "study_blocks")
data class StudyBlock(
    @PrimaryKey val id: String,
    val subjectId: String,
    val subjectName: String,
    val subjectIcon: String,
    val blockNumber: Int,
    val durationMinutes: Int,
    val scheduledDate: LocalDate,
    val isCompleted: Boolean = false,
    val completedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val userId: String,
    val spacedRepetitionInterval: Int = 1, // Days from previous block
    val totalBlocksForSubject: Int = 1, // Total blocks for this subject in current schedule
    val isCustomBlock: Boolean = false // Whether this is a custom block
) {
    val isOverdue: Boolean
        get() = scheduledDate.isBefore(LocalDate.now()) && !isCompleted
    
    val canComplete: Boolean
        get() = scheduledDate <= LocalDate.now() || scheduledDate == LocalDate.now().plusDays(1)
}

enum class StudyBlockStatus {
    PENDING,
    AVAILABLE,
    COMPLETED,
    OVERDUE
}

fun StudyBlock.getStatus(): StudyBlockStatus {
    return when {
        isCompleted -> StudyBlockStatus.COMPLETED
        isOverdue -> StudyBlockStatus.OVERDUE
        canComplete -> StudyBlockStatus.AVAILABLE
        else -> StudyBlockStatus.PENDING
    }
}