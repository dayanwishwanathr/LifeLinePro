package com.ecotracker.lifelinepro.data.models

import java.util.Date

/**
 * Data class representing a mood journal entry
 * @property id Unique identifier for the entry
 * @property emoji Emoji representing the mood
 * @property moodType Type/category of mood (happy, sad, neutral, etc.)
 * @property note Optional note about the mood
 * @property timestamp When the mood was recorded
 * @property date Date string in format "yyyy-MM-dd"
 */
data class MoodEntry(
    val id: String,
    val emoji: String,
    val moodType: String,
    val note: String = "",
    val timestamp: Date,
    val date: String
) {
    companion object {
        // Mood types with their numeric values for trend analysis
        const val MOOD_VERY_HAPPY = "Very Happy"
        const val MOOD_HAPPY = "Happy"
        const val MOOD_NEUTRAL = "Neutral"
        const val MOOD_SAD = "Sad"
        const val MOOD_VERY_SAD = "Very Sad"
        
        /**
         * Convert mood type to numeric value for charting
         */
        fun moodToValue(moodType: String): Float {
            return when (moodType) {
                MOOD_VERY_HAPPY -> 5f
                MOOD_HAPPY -> 4f
                MOOD_NEUTRAL -> 3f
                MOOD_SAD -> 2f
                MOOD_VERY_SAD -> 1f
                else -> 3f
            }
        }
    }
}

