package com.ecotracker.lifelinepro.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ecotracker.lifelinepro.workers.HydrationReminderWorker
import java.util.concurrent.TimeUnit

/**
 * Utility class to schedule hydration reminders using WorkManager
 */
object HydrationReminderScheduler {
    
    /**
     * Schedule periodic hydration reminders
     * @param context Application context
     * @param intervalMinutes Interval between reminders in minutes
     */
    fun scheduleReminder(context: Context, intervalMinutes: Int) {
        // WorkManager requires minimum 15 minutes for periodic work
        val interval = intervalMinutes.toLong().coerceAtLeast(15)
        
        val workRequest = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
            interval,
            TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            HydrationReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    /**
     * Cancel hydration reminders
     * @param context Application context
     */
    fun cancelReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(HydrationReminderWorker.WORK_NAME)
    }
}

