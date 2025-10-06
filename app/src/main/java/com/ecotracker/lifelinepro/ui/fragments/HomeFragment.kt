package com.ecotracker.lifelinepro.ui.fragments

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.ecotracker.lifelinepro.R
import com.ecotracker.lifelinepro.data.models.MoodEntry
import com.ecotracker.lifelinepro.data.models.UserProfile
import com.ecotracker.lifelinepro.data.repository.SharedPreferencesManager
import com.ecotracker.lifelinepro.databinding.FragmentHomeBinding
import com.ecotracker.lifelinepro.databinding.DialogUserProfileBinding
import com.ecotracker.lifelinepro.widget.HabitProgressWidget
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Home Fragment - Dashboard showing overall progress
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefsManager: SharedPreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsManager = SharedPreferencesManager.getInstance(requireContext())
        
        setupHeader()
        setupProfileIcon()
        setupCardNavigation()
        loadOverallProgress()
    }

    private fun setupCardNavigation() {
        // Navigate to Habits page when habits card is clicked
        binding.habitsProgressCard.setOnClickListener {
            navigateToPage(com.ecotracker.lifelinepro.R.id.nav_habits)
        }

        // Navigate to Mood page when mood card is clicked
        binding.moodProgressCard.setOnClickListener {
            navigateToPage(com.ecotracker.lifelinepro.R.id.nav_mood)
        }

        // Navigate to Hydration page when hydration card is clicked
        binding.hydrationProgressCard.setOnClickListener {
            navigateToPage(com.ecotracker.lifelinepro.R.id.nav_hydration)
        }
    }

    private fun navigateToPage(itemId: Int) {
        // Get the MainActivity's bottom navigation and trigger navigation
        val mainActivity = activity as? com.ecotracker.lifelinepro.MainActivity
        mainActivity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
            com.ecotracker.lifelinepro.R.id.bottom_navigation
        )?.selectedItemId = itemId
    }

    private fun setupHeader() {
        // Get current authenticated user
        val currentUser = prefsManager.getCurrentUser()
        val userProfile = prefsManager.getUserProfile()
        
        // Set user name from authenticated user or profile, fallback to "Guest"
        val displayName = when {
            currentUser != null && currentUser.fullName.isNotEmpty() -> currentUser.fullName
            userProfile.fullName.isNotEmpty() -> userProfile.fullName
            else -> "Guest"
        }
        binding.userNameText.text = displayName
        
        // Set greeting based on time of day
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
        binding.greetingText.text = greeting
        
        // Set current date
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        binding.dateText.text = dateFormat.format(Date())
    }

    private fun setupProfileIcon() {
        binding.profileIcon.setOnClickListener {
            showUserProfileDialog()
        }
    }

    private fun showUserProfileDialog() {
        val dialogBinding = DialogUserProfileBinding.inflate(layoutInflater)
        val currentProfile = prefsManager.getUserProfile()
        
        // Load current profile data
        loadProfileData(dialogBinding, currentProfile)
        
        // Setup gender dropdown
        setupGenderDropdown(dialogBinding)
        
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
         dialogBinding.btnSave.setOnClickListener {
             if (saveUserProfile(dialogBinding)) {
                 dialog.dismiss()
                 // Refresh the header to show updated name
                 setupHeader()
                 // Update widget to reflect profile changes
                 HabitProgressWidget.updateAllWidgets(requireContext())
                 android.widget.Toast.makeText(
                     requireContext(),
                     "Profile saved successfully",
                     android.widget.Toast.LENGTH_SHORT
                 ).show()
             }
         }
        
        dialog.show()
    }

    private fun loadOverallProgress() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        
        // Get all data
        val habits = prefsManager.getHabits()
        val moodEntries = prefsManager.getMoodEntries().filter { it.date == today }
        val settings = prefsManager.getUserSettings()
        val waterIntake = prefsManager.getTodayWaterIntake()
        
        // Calculate habits progress
        val completedHabits = habits.count { it.isCompleted }
        val totalHabits = habits.size
        val habitsProgress = if (totalHabits > 0) {
            ((completedHabits.toFloat() / totalHabits) * 100).toInt()
        } else 0
        
        // Calculate hydration progress
        val hydrationProgress = if (settings.dailyWaterGoalMl > 0) {
            ((waterIntake.toFloat() / settings.dailyWaterGoalMl) * 100).toInt().coerceAtMost(100)
        } else 0
        
        // Calculate overall progress (average of habits and hydration)
        val overallProgress = ((habitsProgress + hydrationProgress) / 2)
        
        // Calculate average mood from today's entries
        val averageMoodEmoji = if (moodEntries.isNotEmpty()) {
            val totalMoodValue = moodEntries.sumOf { 
                com.ecotracker.lifelinepro.data.models.MoodEntry.moodToValue(it.moodType).toDouble()
            }
            val averageValue = (totalMoodValue / moodEntries.size).toFloat()
            
            // Convert average back to emoji
            when {
                averageValue >= 4.5f -> "😄" // Very Happy
                averageValue >= 3.5f -> "😊" // Happy
                averageValue >= 2.5f -> "😐" // Neutral
                averageValue >= 1.5f -> "😔" // Sad
                else -> "😢" // Very Sad
            }
        } else {
            "😊" // Default emoji if no mood logged
        }
        
        // Update Overall Stats Card
        binding.totalProgressValue.text = "$overallProgress%"
        binding.habitsCompletedValue.text = "$completedHabits/$totalHabits"
        binding.todayOverallMoodEmoji.text = averageMoodEmoji
        
        // Update Habits Card
        binding.habitsSummaryText.text = if (totalHabits > 0) {
            "$completedHabits of $totalHabits completed"
        } else {
            "No habits tracked today"
        }
        binding.habitsCircularProgress.progress = habitsProgress
        binding.habitsProgressPercentage.text = "$habitsProgress%"
        animateProgress(binding.habitsCircularProgress, habitsProgress)
        
        // Update Mood Card - Show average mood
        if (moodEntries.isNotEmpty()) {
            binding.todayMoodEmoji.text = averageMoodEmoji
            binding.moodSummaryText.text = "${moodEntries.size} ${if (moodEntries.size == 1) "entry" else "entries"} • Avg mood"
        } else {
            binding.todayMoodEmoji.text = "😊"
            binding.moodSummaryText.text = "No mood logged today"
        }
        
        // Update Hydration Card
        binding.hydrationSummaryText.text = "$waterIntake ml of ${settings.dailyWaterGoalMl} ml"
        binding.hydrationCircularProgress.progress = hydrationProgress
        binding.hydrationProgressPercentage.text = "$hydrationProgress%"
        animateProgress(binding.hydrationCircularProgress, hydrationProgress)
    }

    private fun animateProgress(progressBar: android.widget.ProgressBar, targetProgress: Int) {
        // Only animate if there's a significant change to avoid unnecessary animations
        if (kotlin.math.abs(progressBar.progress - targetProgress) > 5) {
            ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, targetProgress)
                .setDuration(400) // Reduced duration for better performance
                .start()
        } else {
            progressBar.progress = targetProgress
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when fragment becomes visible
        if (::prefsManager.isInitialized) {
            setupHeader()
            loadOverallProgress()
            // Update widget when home screen is viewed
            HabitProgressWidget.updateAllWidgets(requireContext())
        }
    }

    private fun loadProfileData(binding: DialogUserProfileBinding, profile: UserProfile) {
        binding.nameInput.setText(profile.fullName)
        binding.emailInput.setText(profile.email)
        binding.genderInput.setText(profile.gender)
        binding.bioInput.setText(profile.bio)
    }
    
    private fun setupGenderDropdown(binding: DialogUserProfileBinding) {
        val genderOptions = arrayOf("Male", "Female", "Other", "Prefer not to say")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genderOptions)
        binding.genderInput.setAdapter(adapter)
    }
    
    
    private fun saveUserProfile(binding: DialogUserProfileBinding): Boolean {
        val fullName = binding.nameInput.text.toString().trim()
        val email = binding.emailInput.text.toString().trim()
        val gender = binding.genderInput.text.toString().trim()
        val bio = binding.bioInput.text.toString().trim()
        
        // Basic validation
        if (fullName.isEmpty()) {
            binding.nameInputLayout.error = "Name is required"
            return false
        }
        
        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = "Invalid email format"
            return false
        }
        
        // Clear errors
        binding.nameInputLayout.error = null
        binding.emailInputLayout.error = null
        
        // Create and save profile
        val profile = UserProfile(
            fullName = fullName,
            email = email,
            gender = gender,
            bio = bio,
            updatedAt = java.util.Date()
        )
        
        prefsManager.saveUserProfile(profile)
        
        // Also update the current authenticated user's full name
        val currentUser = prefsManager.getCurrentUser()
        if (currentUser != null) {
            val updatedUser = currentUser.copy(fullName = fullName)
            prefsManager.updateAuthUser(updatedUser)
            prefsManager.setCurrentUser(updatedUser)
        }
        
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

