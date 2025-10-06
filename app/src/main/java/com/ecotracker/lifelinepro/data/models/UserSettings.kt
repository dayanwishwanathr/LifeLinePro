package com.ecotracker.lifelinepro.data.models

/**
 * Data class representing user preferences and settings
 * @property userName User's display name
 * @property userEmail User's email address
 * @property userPassword User's password (stored for demo purposes only)
 * @property profileImageUri URI of the user's profile image
 * @property dailyWaterGoalMl Daily water intake goal in milliliters
 * @property reminderEnabled Whether hydration reminders are enabled
 * @property reminderIntervalMinutes Interval between reminders in minutes
 * @property notificationsEnabled Whether notifications are enabled
 * @property darkModeEnabled Whether dark mode is enabled
 */
data class UserSettings(
    val userName: String = "Guest User",
    val userEmail: String = "",
    val userPassword: String = "",
    val profileImageUri: String = "",
    val dailyWaterGoalMl: Int = 2000,
    val reminderEnabled: Boolean = true,
    val reminderIntervalMinutes: Int = 60,
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false
)

