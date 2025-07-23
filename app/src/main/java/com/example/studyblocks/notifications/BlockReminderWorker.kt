package com.example.studyblocks.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.studyblocks.repository.StudyRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class BlockReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val studyRepository: StudyRepository,
    private val notificationService: NotificationService
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            val notificationType = inputData.getString("notification_type") ?: "morning"
            val notificationId = inputData.getInt("notification_id", 1001)
            
            // Get current user
            val currentUser = studyRepository.getCurrentUserFlow().first()
            if (currentUser == null) {
                return Result.success()
            }
            
            // Get today's incomplete blocks
            val todayBlocks = studyRepository.getBlocksForDate(currentUser.id, LocalDate.now()).first()
            val remainingBlocks = todayBlocks.filter { !it.isCompleted }
            
            // Show notification if there are remaining blocks
            if (remainingBlocks.isNotEmpty()) {
                notificationService.showBlockReminderNotification(
                    notificationType = notificationType,
                    notificationId = notificationId,
                    remainingBlocks = remainingBlocks
                )
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}