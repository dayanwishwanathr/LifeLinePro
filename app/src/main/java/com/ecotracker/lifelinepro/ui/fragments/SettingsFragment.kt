package com.ecotracker.lifelinepro.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.ecotracker.lifelinepro.R
import com.ecotracker.lifelinepro.data.repository.SharedPreferencesManager
import com.ecotracker.lifelinepro.data.models.UserProfile
import com.ecotracker.lifelinepro.databinding.FragmentSettingsBinding
import com.ecotracker.lifelinepro.databinding.DialogUserProfileBinding
import android.widget.ArrayAdapter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.google.gson.Gson

/**
 * Fragment for managing user settings and preferences
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefsManager: SharedPreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsManager = SharedPreferencesManager.getInstance(requireContext())
        
        loadSettings()
        setupButtons()
    }

    private fun loadSettings() {
        val settings = prefsManager.getUserSettings()
        binding.waterGoalInput.setText(settings.dailyWaterGoalMl.toString())
        binding.notificationsSwitch.isChecked = settings.notificationsEnabled
    }

    private fun setupButtons() {
        binding.btnExportData.setOnClickListener {
            exportData()
        }
        
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
        
        binding.btnUserProfile.setOnClickListener {
            showUserProfileDialog()
        }
        
        // Auto-save when water goal changes
        binding.waterGoalInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveWaterGoal()
            }
        }
        
        // Auto-save when notifications toggle changes
        binding.notificationsSwitch.setOnCheckedChangeListener { _, _ ->
            saveWaterGoal()
        }
    }

    private fun saveWaterGoal() {
        val waterGoalStr = binding.waterGoalInput.text.toString().trim()
        
        val waterGoal = waterGoalStr.toIntOrNull()
        if (waterGoal == null || waterGoal <= 0) {
            binding.waterGoalInput.error = "Please enter valid water goal"
            return
        }
        
        binding.waterGoalInput.error = null
        
        val currentSettings = prefsManager.getUserSettings()
        val updatedSettings = currentSettings.copy(
            dailyWaterGoalMl = waterGoal,
            notificationsEnabled = binding.notificationsSwitch.isChecked
        )
        
        prefsManager.saveUserSettings(updatedSettings)
        
        android.widget.Toast.makeText(
            requireContext(),
            "Settings saved",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    private fun exportData() {
        // Create a summary of all data
        val habits = prefsManager.getHabits()
        val moodEntries = prefsManager.getMoodEntries()
        val hydrationIntakes = prefsManager.getHydrationIntakes()
        val settings = prefsManager.getUserSettings()
        
        val exportData = mapOf(
            "habits" to habits,
            "moodEntries" to moodEntries,
            "hydrationIntakes" to hydrationIntakes,
            "settings" to settings,
            "exportDate" to java.util.Date().toString()
        )
        
        val gson = Gson()
        val jsonData = gson.toJson(exportData)
        
        // Share data as text
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "LifeLinePro Data Export")
            putExtra(Intent.EXTRA_TEXT, jsonData)
        }
        
        startActivity(Intent.createChooser(shareIntent, "Export Data"))
        
        android.widget.Toast.makeText(
            requireContext(),
            "Data exported successfully",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    private fun showClearDataConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.clear_all_data)
            .setMessage(R.string.clear_data_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                clearAllData()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun clearAllData() {
        prefsManager.clearAllData()
        
        // Reinitialize with default settings
        prefsManager.saveUserSettings(com.ecotracker.lifelinepro.data.models.UserSettings())
        
        loadSettings()
        
        android.widget.Toast.makeText(
            requireContext(),
            "All data cleared",
            android.widget.Toast.LENGTH_SHORT
        ).show()
        
        // Refresh the UI by reloading the fragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, SettingsFragment())
            .commit()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout & Clear Data")
            .setMessage("Are you sure you want to logout? This will permanently delete all your data including:\n\n• Habits and progress\n• Mood entries\n• Hydration records\n• User profile\n• Settings\n\nThis action cannot be undone.")
            .setPositiveButton("Logout & Clear") { _, _ ->
                performLogout()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun performLogout() {
        // Clear all data (habits, moods, hydration, settings, profiles, etc.)
        prefsManager.clearAllData()
        
        // Also logout current user
        prefsManager.logout()
        
        android.widget.Toast.makeText(
            requireContext(),
            "Logged out and all data cleared",
            android.widget.Toast.LENGTH_SHORT
        ).show()
        
        // Navigate to AuthActivity
        val intent = android.content.Intent(requireContext(), com.ecotracker.lifelinepro.AuthActivity::class.java)
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
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
                android.widget.Toast.makeText(
                    requireContext(),
                    "Profile saved successfully",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
        
        dialog.show()
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
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

