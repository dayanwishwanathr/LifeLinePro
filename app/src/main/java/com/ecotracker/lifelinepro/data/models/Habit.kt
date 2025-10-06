package com.ecotracker.lifelinepro.data.models

import java.util.Date

/**
 * Data class representing a daily wellness habit
 * @property id Unique identifier for the habit
 * @property name Name of the habit (e.g., "Drink Water", "Meditate")
 * @property description Optional description for the habit
 * @property icon Icon resource name for display
 * @property targetCount Daily target count for the habit
 * @property currentCount Current completion count for today
 * @property unit Unit of measurement (e.g., "times", "glasses", "hours")
 * @property category Category of the habit (e.g., "Health", "Personal", "Fitness")
 * @property lastUpdated Last time the habit was updated
 * @property isCompleted Whether the habit is completed for today
 */
data class Habit(
    val id: String,
    val name: String,
    val description: String = "",
    val icon: String,
    val targetCount: Int,
    var currentCount: Int = 0,
    val unit: String = "times",
    val category: String = "Personal",
    var lastUpdated: Date = Date(),
    var isCompleted: Boolean = false
) {
    /**
     * Calculate completion percentage
     */
    fun getCompletionPercentage(): Int {
        return if (targetCount > 0) {
            ((currentCount.toFloat() / targetCount) * 100).toInt().coerceAtMost(100)
        } else 0
    }
}

