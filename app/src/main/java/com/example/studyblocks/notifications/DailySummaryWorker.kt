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
class DailySummaryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val studyRepository: StudyRepository,
    private val notificationService: NotificationService
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            android.util.Log.d("DailySummaryWorker", "Starting daily summary worker at ${LocalDate.now()}")
            
            // Get current user
            val currentUser = studyRepository.getCurrentUserFlow().first()
            if (currentUser == null) {
                android.util.Log.w("DailySummaryWorker", "No current user found, skipping daily summary")
                return Result.success()
            }
            
            android.util.Log.d("DailySummaryWorker", "Processing daily summary for user: ${currentUser.id}")
            
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            val tomorrow = today.plusDays(1)
            
            // Get yesterday's blocks for summary
            val yesterdayBlocks = studyRepository.getBlocksForDate(currentUser.id, yesterday).first()
            val completedBlocks = yesterdayBlocks.count { it.isCompleted }
            val totalBlocks = yesterdayBlocks.size
            
            android.util.Log.d("DailySummaryWorker", "Yesterday (${yesterday}): $completedBlocks/$totalBlocks blocks completed")
            
            // Get tomorrow's blocks
            val tomorrowBlocks = studyRepository.getBlocksForDate(currentUser.id, tomorrow).first()
            android.util.Log.d("DailySummaryWorker", "Tomorrow (${tomorrow}): ${tomorrowBlocks.size} blocks scheduled")
            
            // Calculate missed blocks (this is simplified - in real app you'd track rescheduled vs missed)
            val missedBlocks = yesterdayBlocks.count { !it.isCompleted }
            
            // Show notification and set pending summary flag if there's meaningful data to show
            if (totalBlocks > 0 || tomorrowBlocks.isNotEmpty()) {
                android.util.Log.d("DailySummaryWorker", "Setting pending summary date and showing notification")
                
                // Set the pending summary date for yesterday (the date we're summarizing)
                try {
                    studyRepository.setPendingSummaryDate(currentUser.id, yesterday.toString())
                } catch (e: Exception) {
                    android.util.Log.e("DailySummaryWorker", "Error setting pending summary date", e)
                }
                
                // Show notification
                notificationService.showDailySummaryNotification(
                    completedBlocks = completedBlocks,
                    totalBlocks = totalBlocks,
                    rescheduledBlocks = 0, // This would need to be tracked in the data model
                    missedBlocks = missedBlocks,
                    tomorrowBlocks = tomorrowBlocks.size
                )
            } else {
                android.util.Log.d("DailySummaryWorker", "No meaningful data to show, skipping notification")
            }
            
            android.util.Log.d("DailySummaryWorker", "Daily summary worker completed successfully")
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("DailySummaryWorker", "Daily summary worker failed", e)
            Result.failure()
        }
    }
}