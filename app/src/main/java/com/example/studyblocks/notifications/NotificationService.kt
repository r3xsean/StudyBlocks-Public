package com.example.studyblocks.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.example.studyblocks.MainActivity
import com.example.studyblocks.R
import com.example.studyblocks.data.model.StudyBlock
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        const val CHANNEL_ID = "study_blocks_channel"
        const val NOTIFICATION_ID_MORNING = 1001
        const val NOTIFICATION_ID_AFTERNOON = 1002
        const val NOTIFICATION_ID_EVENING = 1003
        const val NOTIFICATION_ID_DAILY_SUMMARY = 1004
        
        const val WORK_TAG_MORNING = "morning_reminder"
        const val WORK_TAG_AFTERNOON = "afternoon_reminder"
        const val WORK_TAG_EVENING = "evening_reminder"
        const val WORK_TAG_DAILY_SUMMARY = "daily_summary"
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
    
    fun scheduleDailySummary(enabled: Boolean = true) {
        val workManager = WorkManager.getInstance(context)
        
        android.util.Log.d("NotificationService", "Scheduling daily summary notifications: enabled=$enabled")
        
        // Cancel existing daily summary work
        workManager.cancelAllWorkByTag(WORK_TAG_DAILY_SUMMARY)
        
        if (enabled) {
            val delayToMidnight = calculateDelayToTime(0, 0)
            android.util.Log.d("NotificationService", "Daily summary scheduled with delay: ${delayToMidnight}ms (${delayToMidnight / (1000 * 60 * 60)} hours)")
            
            val summaryWork = PeriodicWorkRequestBuilder<DailySummaryWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delayToMidnight, TimeUnit.MILLISECONDS) // Midnight
                .setInputData(
                    Data.Builder()
                        .putString("notification_type", "daily_summary")
                        .putInt("notification_id", NOTIFICATION_ID_DAILY_SUMMARY)
                        .build()
                )
                .addTag(WORK_TAG_DAILY_SUMMARY)
                .build()
            
            workManager.enqueue(summaryWork)
            android.util.Log.d("NotificationService", "Daily summary work enqueued successfully")
        } else {
            android.util.Log.d("NotificationService", "Daily summary notifications disabled")
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
        workManager.cancelAllWorkByTag(WORK_TAG_DAILY_SUMMARY)
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
    
    fun showDailySummaryNotification(
        completedBlocks: Int,
        totalBlocks: Int,
        rescheduledBlocks: Int,
        missedBlocks: Int,
        tomorrowBlocks: Int
    ) {
        android.util.Log.d("NotificationService", "Showing daily summary notification: $completedBlocks/$totalBlocks completed, $missedBlocks missed, $tomorrowBlocks tomorrow")
        val title = "📊 Daily Study Summary"
        
        val message = buildString {
            if (completedBlocks > 0) {
                append("✅ Completed $completedBlocks/${totalBlocks} blocks")
            } else {
                append("No blocks completed today")
            }
            
            if (rescheduledBlocks > 0) {
                append("\n🔄 Rescheduled $rescheduledBlocks blocks")
            }
            
            if (missedBlocks > 0) {
                append("\n⏰ Missed $missedBlocks blocks (moved to tomorrow)")
            }
            
            if (tomorrowBlocks > 0) {
                append("\n🌅 $tomorrowBlocks blocks scheduled for tomorrow")
            }
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
            .setContentText("Tap to see your study progress")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID_DAILY_SUMMARY, builder.build())
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
    
    // Test method to manually trigger daily summary worker immediately (for testing)
    fun testTriggerDailySummary() {
        android.util.Log.d("NotificationService", "Test: Manually triggering daily summary worker")
        val workManager = WorkManager.getInstance(context)
        
        val testSummaryWork = OneTimeWorkRequestBuilder<DailySummaryWorker>()
            .setInputData(
                Data.Builder()
                    .putString("notification_type", "daily_summary_test")
                    .putInt("notification_id", NOTIFICATION_ID_DAILY_SUMMARY)
                    .build()
            )
            .addTag("daily_summary_test")
            .build()
        
        workManager.enqueue(testSummaryWork)
        android.util.Log.d("NotificationService", "Test: Daily summary work enqueued for immediate execution")
    }
    
    // Permission checking methods
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // For Android 12 and below, notification permission is granted by default
            true
        }
    }
    
    fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled() && hasNotificationPermission()
    }
    
    // Test method to show a simple notification for testing permissions
    fun testShowSimpleNotification() {
        android.util.Log.d("NotificationService", "Test: Attempting to show simple test notification")
        
        val hasPermission = hasNotificationPermission()
        android.util.Log.d("NotificationService", "Test: hasNotificationPermission() = $hasPermission")
        android.util.Log.d("NotificationService", "Test: Android SDK version = ${Build.VERSION.SDK_INT}")
        
        if (!hasPermission) {
            android.util.Log.w("NotificationService", "Test: No notification permission granted, but continuing to attempt notification")
            // On Android 13+, attempting to show notification without permission may trigger system dialog
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
            .setContentTitle("🧪 Test Notification")
            .setContentText("Notification permissions are working!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        with(NotificationManagerCompat.from(context)) {
            try {
                android.util.Log.d("NotificationService", "Test: About to call notify() with notification ID 9999")
                notify(9999, builder.build()) // Use a unique ID for test notifications
                android.util.Log.d("NotificationService", "Test: notify() call completed successfully")
            } catch (e: SecurityException) {
                android.util.Log.e("NotificationService", "Test: SecurityException when posting notification", e)
            } catch (e: Exception) {
                android.util.Log.e("NotificationService", "Test: Unexpected exception when posting notification", e)
            }
        }
    }
}