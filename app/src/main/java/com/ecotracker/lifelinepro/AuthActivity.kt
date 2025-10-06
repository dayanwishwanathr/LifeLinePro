package com.ecotracker.lifelinepro

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.ecotracker.lifelinepro.data.models.AuthUser
import com.ecotracker.lifelinepro.data.models.LoginRequest
import com.ecotracker.lifelinepro.data.models.RegisterRequest
import com.ecotracker.lifelinepro.data.repository.SharedPreferencesManager
import com.ecotracker.lifelinepro.databinding.ActivityAuthBinding
import java.util.UUID

/**
 * Authentication Activity for login and register functionality
 */
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var prefsManager: SharedPreferencesManager
    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = SharedPreferencesManager.getInstance(this)
        
        setupUI()
        setupClickListeners()
        setupAnimations()
    }

    private fun setupUI() {
        // Set initial state for login mode
        updateUIForMode(isLoginMode)
    }

    private fun setupClickListeners() {
        binding.primaryActionButton.setOnClickListener {
            if (isLoginMode) {
                performLogin()
            } else {
                performRegister()
            }
        }

        binding.secondaryActionButton.setOnClickListener {
            toggleMode()
        }

        binding.skipButton.setOnClickListener {
            skipAuthentication()
        }
    }

    private fun setupAnimations() {
        // Animate logo
        val logoAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        binding.authLogo.startAnimation(logoAnimation)

        // Animate card
        val cardAnimation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        cardAnimation.startOffset = 300
        binding.authCard.startAnimation(cardAnimation)
    }

    private fun toggleMode() {
        isLoginMode = !isLoginMode
        updateUIForMode(isLoginMode)
        
        // Clear all input fields
        clearInputFields()
        
        // Animate the transition
        val slideAnimation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        binding.authCard.startAnimation(slideAnimation)
    }

    private fun updateUIForMode(isLogin: Boolean) {
        if (isLogin) {
            // Login mode
            binding.authTitle.text = getString(R.string.welcome_back)
            binding.primaryActionButton.text = getString(R.string.login)
            binding.secondaryActionButton.text = getString(R.string.dont_have_account)
            
            // Hide register-specific fields
            binding.fullNameInputLayout.visibility = View.GONE
            binding.confirmPasswordInputLayout.visibility = View.GONE
        } else {
            // Register mode
            binding.authTitle.text = getString(R.string.create_account)
            binding.primaryActionButton.text = getString(R.string.register)
            binding.secondaryActionButton.text = getString(R.string.already_have_account)
            
            // Show register-specific fields
            binding.fullNameInputLayout.visibility = View.VISIBLE
            binding.confirmPasswordInputLayout.visibility = View.VISIBLE
        }
    }

    private fun clearInputFields() {
        binding.emailInput.text?.clear()
        binding.passwordInput.text?.clear()
        binding.fullNameInput.text?.clear()
        binding.confirmPasswordInput.text?.clear()
        
        // Clear errors
        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null
        binding.fullNameInputLayout.error = null
        binding.confirmPasswordInputLayout.error = null
    }

    private fun performLogin() {
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill all fields")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = "Invalid email format"
            return
        }

        // Clear errors
        binding.emailInputLayout.error = null

        // Perform login
        val loginRequest = LoginRequest(email, password)
        val result = authenticateUser(loginRequest)

        if (result.success) {
            // Login successful
            android.widget.Toast.makeText(this, result.message, android.widget.Toast.LENGTH_SHORT).show()
            navigateToMainApp()
        } else {
            // Login failed
            showError(result.message)
        }
    }

    private fun performRegister() {
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()
        val confirmPassword = binding.confirmPasswordInput.text.toString().trim()
        val fullName = binding.fullNameInput.text.toString().trim()

        // Validation
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || fullName.isEmpty()) {
            showError("Please fill all fields")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = "Invalid email format"
            return
        }

        if (password != confirmPassword) {
            binding.confirmPasswordInputLayout.error = "Passwords don't match"
            return
        }

        if (password.length < 6) {
            binding.passwordInputLayout.error = "Password must be at least 6 characters"
            return
        }

        // Clear errors
        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null
        binding.confirmPasswordInputLayout.error = null

        // Perform registration
        val registerRequest = RegisterRequest(email, password, confirmPassword, fullName)
        val result = registerUser(registerRequest)

        if (result.success) {
            // Registration successful
            android.widget.Toast.makeText(this, result.message, android.widget.Toast.LENGTH_SHORT).show()
            // Automatically create user profile with registration data
            createUserProfileFromRegistration()
            navigateToMainApp()
        } else {
            // Registration failed
            showError(result.message)
        }
    }

    private fun authenticateUser(loginRequest: LoginRequest): com.ecotracker.lifelinepro.data.models.AuthResponse {
        val users = prefsManager.getAuthUsers()
        val user = users.find { it.email == loginRequest.email && it.password == loginRequest.password }

        return if (user != null) {
            // Update last login time
            val updatedUser = user.copy(lastLoginAt = java.util.Date())
            prefsManager.updateAuthUser(updatedUser)
            prefsManager.setCurrentUser(updatedUser)
            
            com.ecotracker.lifelinepro.data.models.AuthResponse(
                success = true,
                message = getString(R.string.login_successful),
                user = updatedUser
            )
        } else {
            com.ecotracker.lifelinepro.data.models.AuthResponse(
                success = false,
                message = getString(R.string.invalid_credentials)
            )
        }
    }

    private fun registerUser(registerRequest: RegisterRequest): com.ecotracker.lifelinepro.data.models.AuthResponse {
        val users = prefsManager.getAuthUsers()
        
        // Check if email already exists
        if (users.any { it.email == registerRequest.email }) {
            return com.ecotracker.lifelinepro.data.models.AuthResponse(
                success = false,
                message = getString(R.string.email_already_exists)
            )
        }

        // Create new user
        val newUser = AuthUser(
            id = UUID.randomUUID().toString(),
            email = registerRequest.email,
            password = registerRequest.password, // Note: In production, hash this
            fullName = registerRequest.fullName,
            createdAt = java.util.Date(),
            lastLoginAt = java.util.Date(),
            isEmailVerified = false
        )

        // Save user
        prefsManager.addAuthUser(newUser)
        prefsManager.setCurrentUser(newUser)

        return com.ecotracker.lifelinepro.data.models.AuthResponse(
            success = true,
            message = getString(R.string.register_successful),
            user = newUser
        )
    }

    private fun skipAuthentication() {
        // Create a guest user
        val guestUser = AuthUser(
            id = "guest",
            email = "guest@lifelinepro.com",
            password = "",
            fullName = "Guest User",
            createdAt = java.util.Date(),
            lastLoginAt = java.util.Date(),
            isEmailVerified = false
        )
        
        prefsManager.setCurrentUser(guestUser)
        navigateToMainApp()
    }

    private fun navigateToMainApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun createUserProfileFromRegistration() {
        val currentUser = prefsManager.getCurrentUser()
        
        if (currentUser != null) {
            // Create user profile with registration data
            val profile = com.ecotracker.lifelinepro.data.models.UserProfile(
                fullName = currentUser.fullName,
                email = currentUser.email,
                gender = "", // Empty - user can fill later
                bio = "", // Empty - user can fill later
                updatedAt = java.util.Date()
            )
            
            // Save the profile
            prefsManager.saveUserProfile(profile)
        }
    }

    private fun showError(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
    }
}
