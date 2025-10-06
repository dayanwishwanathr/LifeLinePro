package com.ecotracker.lifelinepro.data.models

import java.util.Date

/**
 * Data class representing a hydration intake record
 * @property id Unique identifier
 * @property amountMl Amount of water in milliliters
 * @property timestamp When the water was logged
 * @property date Date string in format "yyyy-MM-dd"
 */
data class HydrationIntake(
    val id: String = "",
    val amountMl: Int,
    val timestamp: Date,
    val date: String
)

