package com.ecotracker.lifelinepro.data.models

import java.util.Date

data class UserProfile(
    val fullName: String = "",
    val email: String = "",
    val gender: String = "",
    val bio: String = "",
    val profileImageUri: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
