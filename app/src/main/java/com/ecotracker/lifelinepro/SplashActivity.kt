package com.ecotracker.lifelinepro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.ecotracker.lifelinepro.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    
    companion object {
        private const val SPLASH_DELAY = 2000L // 2 seconds
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide action bar for full screen experience
        supportActionBar?.hide()

        // Start logo animation
        startLogoAnimation()

        // Navigate to next screen after delay
        navigateToNextScreen()
    }

    private fun startLogoAnimation() {
        // Fade in animation for logo
        binding.logoImageView.alpha = 0f
        binding.logoImageView.animate()
            .alpha(1f)
            .setDuration(1000)
            .start()

        // Scale animation for logo
        binding.logoImageView.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(800)
            .setStartDelay(200)
            .withEndAction {
                binding.logoImageView.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(400)
                    .start()
            }
            .start()
    }

    private fun navigateToNextScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
            // Check if onboarding has been completed
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val isOnboardingCompleted = prefs.getBoolean("onboarding_completed", false)
            
            // Check if user is logged in
            val prefsManager = com.ecotracker.lifelinepro.data.repository.SharedPreferencesManager.getInstance(this)
            val isLoggedIn = prefsManager.isUserLoggedIn()

            val intent = when {
                !isOnboardingCompleted -> {
                    // Show onboarding first
                    Intent(this, OnboardingActivity::class.java)
                }
                !isLoggedIn -> {
                    // Show authentication
                    Intent(this, com.ecotracker.lifelinepro.AuthActivity::class.java)
                }
                else -> {
                    // Go directly to MainActivity
                    Intent(this, MainActivity::class.java)
                }
            }

            startActivity(intent)
            finish()
        }, SPLASH_DELAY)
    }

    override fun onBackPressed() {
        // Disable back button during splash screen
        // Do nothing
    }
}
