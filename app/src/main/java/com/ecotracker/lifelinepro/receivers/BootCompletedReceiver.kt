package com.ecotracker.lifelinepro.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ecotracker.lifelinepro.data.repository.SharedPreferencesManager
import com.ecotracker.lifelinepro.utils.HydrationReminderScheduler

/**
 * Receiver that reschedules hydration reminders after device reboot
 */
class BootCompletedReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefsManager = SharedPreferencesManager.getInstance(context)
            val settings = prefsManager.getUserSettings()
            
            if (settings.reminderEnabled) {
                HydrationReminderScheduler.scheduleReminder(
                    context,
                    settings.reminderIntervalMinutes
                )
            }
        }
    }
}

