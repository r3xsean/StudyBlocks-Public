package com.example.studyblocks.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.example.studyblocks.MainActivity
import com.example.studyblocks.R
import com.example.studyblocks.data.model.StudyBlock
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    private val context: Context
) {
    
    companion object {
        const val CHANNEL_ID = "study_blocks_channel"
        const val NOTIFICATION_ID_MORNING = 1001
        const val NOTIFICATION_ID_AFTERNOON = 1002
        const val NOTIFICATION_ID_EVENING = 1003
        
        const val WORK_TAG_MORNING = "morning_reminder"
        const val WORK_TAG_AFTERNOON = "afternoon_reminder"
        const val WORK_TAG_EVENING = "evening_reminder"
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Study Block Reminders"
            val descriptionText = "Notifications for remaining study blocks"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun scheduleBlockReminders(
        morningEnabled: Boolean = true,
        afternoonEnabled: Boolean = true,
        eveningEnabled: Boolean = true
    ) {
        val workManager = WorkManager.getInstance(context)
        
        // Cancel existing work
        workManager.cancelAllWorkByTag(WORK_TAG_MORNING)
        workManager.cancelAllWorkByTag(WORK_TAG_AFTERNOON)
        workManager.cancelAllWorkByTag(WORK_TAG_EVENING)
        
        // Schedule morning reminder (8:00 AM)
        if (morningEnabled) {
            val morningWork = PeriodicWorkRequestBuilder<BlockReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(calculateDelayToTime(8, 0), TimeUnit.MILLISECONDS)
                .setInputData(
                    Data.Builder()
                        .putString("notification_type", "morning")
                        .putInt("notification_id", NOTIFICATION_ID_MORNING)
                        .build()
                )
                .addTag(WORK_TAG_MORNING)
                .build()
            
            workManager.enqueue(morningWork)
        }
        
        // Schedule afternoon reminder (2:00 PM)
        if (afternoonEnabled) {
            val afternoonWork = PeriodicWorkRequestBuilder<BlockReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(calculateDelayToTime(14, 0), TimeUnit.MILLISECONDS)
                .setInputData(
                    Data.Builder()
                        .putString("notification_type", "afternoon")
                        .putInt("notification_id", NOTIFICATION_ID_AFTERNOON)
                        .build()
                )
                .addTag(WORK_TAG_AFTERNOON)
                .build()
            
            workManager.enqueue(afternoonWork)
        }
        
        // Schedule evening reminder (7:00 PM)
        if (eveningEnabled) {
            val eveningWork = PeriodicWorkRequestBuilder<BlockReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(calculateDelayToTime(19, 0), TimeUnit.MILLISECONDS)
                .setInputData(
                    Data.Builder()
                        .putString("notification_type", "evening")
                        .putInt("notification_id", NOTIFICATION_ID_EVENING)
                        .build()
                )
                .addTag(WORK_TAG_EVENING)
                .build()
            
            workManager.enqueue(eveningWork)
        }
    }
    
    fun cancelAllReminders() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(WORK_TAG_MORNING)
        workManager.cancelAllWorkByTag(WORK_TAG_AFTERNOON)
        workManager.cancelAllWorkByTag(WORK_TAG_EVENING)
    }
    
    fun showBlockReminderNotification(
        notificationType: String,
        notificationId: Int,
        remainingBlocks: List<StudyBlock>
    ) {
        if (remainingBlocks.isEmpty()) return
        
        val title = when (notificationType) {
            "morning" -> "Good morning! 🌅"
            "afternoon" -> "Afternoon check-in 📚"
            "evening" -> "Evening review 🌙"
            else -> "Study Reminder"
        }
        
        val message = when (remainingBlocks.size) {
            1 -> "You have 1 study block remaining today"
            else -> "You have ${remainingBlocks.size} study blocks remaining today"
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Add expanded text with subject details
        if (remainingBlocks.size <= 3) {
            val bigText = buildString {
                append(message)
                append("\n\n")
                remainingBlocks.forEach { block ->
                    append("${block.subjectIcon} ${block.subjectName} (${block.durationMinutes}m)\n")
                }
            }.trim()
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
        }
        
        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notificationId, builder.build())
            } catch (e: SecurityException) {
                // Handle notification permission denied
            }
        }
    }
    
    private fun calculateDelayToTime(hour: Int, minute: Int): Long {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            
            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis <= now) {
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        return calendar.timeInMillis - now
    }
}