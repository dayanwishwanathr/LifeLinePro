package com.ecotracker.lifelinepro.data.models

import java.util.Date

/**
 * Authentication User model for login/register functionality
 */
data class AuthUser(
    val id: String = "",
    val email: String = "",
    val password: String = "", // Note: In production, this should be hashed
    val fullName: String = "",
    val createdAt: Date = Date(),
    val lastLoginAt: Date = Date(),
    val isEmailVerified: Boolean = false
)

/**
 * Login request model
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Register request model
 */
data class RegisterRequest(
    val email: String,
    val password: String,
    val confirmPassword: String,
    val fullName: String
)

/**
 * Authentication response model
 */
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: AuthUser? = null
)
