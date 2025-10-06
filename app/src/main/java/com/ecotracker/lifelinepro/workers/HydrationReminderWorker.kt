package com.ecotracker.lifelinepro.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ecotracker.lifelinepro.MainActivity
import com.ecotracker.lifelinepro.R

/**
 * Worker that shows hydration reminder notifications
 */
class HydrationReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        showNotification()
        return Result.success()
    }

    private fun showNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                applicationContext.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = applicationContext.getString(R.string.notification_channel_description)
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open app when notification is clicked
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_hydration", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Quick add water action
        val quickAddIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_hydration", true)
            putExtra("quick_add_water", true)
        }

        val quickAddPendingIntent = PendingIntent.getActivity(
            applicationContext,
            1,
            quickAddIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build enhanced notification
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_hydration)
            .setContentTitle(applicationContext.getString(R.string.hydration_reminder_title))
            .setContentText(applicationContext.getString(R.string.hydration_reminder_message))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Stay hydrated! Drinking water regularly helps you stay healthy and energized. 💧"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0x3B82F6) // Water blue color
            .addAction(
                R.drawable.ic_hydration,
                "Add 250ml",
                quickAddPendingIntent
            )
            .addAction(
                R.drawable.ic_calendar,
                "View",
                pendingIntent
            )
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "hydration_reminder_channel"
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "hydration_reminder_work"
    }
}

