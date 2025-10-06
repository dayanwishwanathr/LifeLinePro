package com.ecotracker.lifelinepro.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.ecotracker.lifelinepro.data.models.Habit
import com.ecotracker.lifelinepro.data.models.HydrationIntake
import com.ecotracker.lifelinepro.data.models.MoodEntry
import com.ecotracker.lifelinepro.data.models.UserSettings
import com.ecotracker.lifelinepro.data.models.UserProfile
import com.ecotracker.lifelinepro.data.models.AuthUser
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Manager class for handling data persistence using SharedPreferences
 * Implements singleton pattern to ensure single instance across the app
 */
class SharedPreferencesManager private constructor(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    companion object {
        private const val PREFS_NAME = "LifeLineProPrefs"
        private const val KEY_HABITS = "habits"
        private const val KEY_MOOD_ENTRIES = "mood_entries"
        private const val KEY_HYDRATION_INTAKES = "hydration_intakes"
        private const val KEY_USER_SETTINGS = "user_settings"
        private const val KEY_USER_PROFILE = "user_profile"
        private const val KEY_AUTH_USERS = "auth_users"
        private const val KEY_CURRENT_USER = "current_user"
        private const val KEY_LAST_HABIT_RESET = "last_habit_reset"
        
        @Volatile
        private var instance: SharedPreferencesManager? = null
        
        fun getInstance(context: Context): SharedPreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: SharedPreferencesManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    // ========== Habit Management ==========
    
    /**
     * Get all habits
     */
    fun getHabits(): List<Habit> {
        val json = prefs.getString(KEY_HABITS, null) ?: return emptyList()
        val type = object : TypeToken<List<Habit>>() {}.type
        val habits: List<Habit> = gson.fromJson(json, type)
        return checkAndResetDailyProgress(habits)
    }
    
    /**
     * Save all habits
     */
    fun saveHabits(habits: List<Habit>) {
        val json = gson.toJson(habits)
        prefs.edit().putString(KEY_HABITS, json).apply()
    }
    
    /**
     * Add a new habit
     */
    fun addHabit(habit: Habit) {
        val habits = getHabits().toMutableList()
        habits.add(habit)
        saveHabits(habits)
    }
    
    /**
     * Update an existing habit
     */
    fun updateHabit(updatedHabit: Habit) {
        val habits = getHabits().toMutableList()
        val index = habits.indexOfFirst { it.id == updatedHabit.id }
        if (index != -1) {
            habits[index] = updatedHabit
            saveHabits(habits)
        }
    }
    
    /**
     * Delete a habit
     */
    fun deleteHabit(habitId: String) {
        val habits = getHabits().toMutableList()
        habits.removeAll { it.id == habitId }
        saveHabits(habits)
    }
    
    /**
     * Check if it's a new day and reset habit progress if needed
     */
    private fun checkAndResetDailyProgress(habits: List<Habit>): List<Habit> {
        val today = dateFormat.format(Date())
        val lastReset = prefs.getString(KEY_LAST_HABIT_RESET, "")
        
        return if (lastReset != today) {
            // Reset all habit counts for the new day
            val resetHabits = habits.map { habit ->
                habit.copy(currentCount = 0, isCompleted = false, lastUpdated = Date())
            }
            saveHabits(resetHabits)
            prefs.edit().putString(KEY_LAST_HABIT_RESET, today).apply()
            resetHabits
        } else {
            habits
        }
    }
    
    // ========== Mood Entry Management ==========
    
    /**
     * Get all mood entries
     */
    fun getMoodEntries(): List<MoodEntry> {
        val json = prefs.getString(KEY_MOOD_ENTRIES, null) ?: return emptyList()
        val type = object : TypeToken<List<MoodEntry>>() {}.type
        return gson.fromJson(json, type)
    }

    /**
     * Get mood entries for the last 7 days
     */
    fun getMoodEntriesForLastWeek(): List<MoodEntry> {
        val allEntries = getMoodEntries()
        val calendar = Calendar.getInstance()
        val today = calendar.time
        
        // Go back 7 days
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val weekAgo = calendar.time
        
        return allEntries.filter { entry ->
            entry.timestamp.after(weekAgo) && entry.timestamp.before(today) || 
            entry.timestamp == today
        }.sortedBy { it.timestamp }
    }
    
    /**
     * Save all mood entries
     */
    fun saveMoodEntries(entries: List<MoodEntry>) {
        val json = gson.toJson(entries)
        prefs.edit().putString(KEY_MOOD_ENTRIES, json).apply()
    }
    
    /**
     * Add a new mood entry
     */
    fun addMoodEntry(entry: MoodEntry) {
        val entries = getMoodEntries().toMutableList()
        entries.add(0, entry) // Add to beginning for chronological order
        saveMoodEntries(entries)
    }
    
    /**
     * Update an existing mood entry
     */
    fun updateMoodEntry(updatedEntry: MoodEntry) {
        val entries = getMoodEntries().toMutableList()
        val index = entries.indexOfFirst { it.id == updatedEntry.id }
        if (index != -1) {
            entries[index] = updatedEntry
            saveMoodEntries(entries)
        }
    }
    
    /**
     * Delete a mood entry
     */
    fun deleteMoodEntry(entryId: String) {
        val entries = getMoodEntries().toMutableList()
        entries.removeAll { it.id == entryId }
        saveMoodEntries(entries)
    }
    
    
    // ========== Hydration Management ==========
    
    /**
     * Get all hydration intakes
     */
    fun getHydrationIntakes(): List<HydrationIntake> {
        val json = prefs.getString(KEY_HYDRATION_INTAKES, null) ?: return emptyList()
        val type = object : TypeToken<List<HydrationIntake>>() {}.type
        return gson.fromJson(json, type)
    }
    
    /**
     * Save all hydration intakes
     */
    fun saveHydrationIntakes(intakes: List<HydrationIntake>) {
        val json = gson.toJson(intakes)
        prefs.edit().putString(KEY_HYDRATION_INTAKES, json).apply()
    }
    
    /**
     * Add a new hydration intake
     */
    fun addHydrationIntake(intake: HydrationIntake) {
        val intakes = getHydrationIntakes().toMutableList()
        val newIntake = if (intake.id.isEmpty()) {
            intake.copy(id = UUID.randomUUID().toString())
        } else {
            intake
        }
        intakes.add(0, newIntake)
        saveHydrationIntakes(intakes)
    }
    
    /**
     * Get today's total water intake
     */
    fun getTodayWaterIntake(): Int {
        val today = dateFormat.format(Date())
        return getHydrationIntakes()
            .filter { it.date == today }
            .sumOf { it.amountMl }
    }
    
    /**
     * Delete a hydration intake
     */
    fun deleteHydrationIntake(intakeId: String) {
        val intakes = getHydrationIntakes().toMutableList()
        intakes.removeAll { it.id == intakeId }
        saveHydrationIntakes(intakes)
    }
    
    // ========== User Settings Management ==========
    
    /**
     * Get user settings
     */
    fun getUserSettings(): UserSettings {
        val json = prefs.getString(KEY_USER_SETTINGS, null)
        return if (json != null) {
            gson.fromJson(json, UserSettings::class.java)
        } else {
            UserSettings() // Return default settings
        }
    }
    
    /**
     * Save user settings
     */
    fun saveUserSettings(settings: UserSettings) {
        val json = gson.toJson(settings)
        prefs.edit().putString(KEY_USER_SETTINGS, json).apply()
    }
    
    // ========== Statistics ==========
    
    /**
     * Get overall habit completion percentage for today
     */
    fun getTodayHabitCompletionPercentage(): Int {
        val habits = getHabits()
        if (habits.isEmpty()) return 0
        
        val totalProgress = habits.sumOf { it.getCompletionPercentage() }
        return totalProgress / habits.size
    }
    
    /**
     * Get count of completed habits today
     */
    fun getCompletedHabitsCount(): Int {
        return getHabits().count { it.isCompleted }
    }
    
    // ========== User Profile Management ==========
    
    /**
     * Save user profile
     */
    fun saveUserProfile(profile: UserProfile) {
        val json = gson.toJson(profile)
        prefs.edit().putString(KEY_USER_PROFILE, json).apply()
    }
    
    /**
     * Get user profile
     */
    fun getUserProfile(): UserProfile {
        val json = prefs.getString(KEY_USER_PROFILE, null)
        return if (json != null) {
            gson.fromJson(json, UserProfile::class.java)
        } else {
            UserProfile() // Return default profile
        }
    }

    // ========== Authentication Management ==========

    /**
     * Get all registered users
     */
    fun getAuthUsers(): List<AuthUser> {
        val json = prefs.getString(KEY_AUTH_USERS, null)
        return if (json != null) {
            val type = object : TypeToken<List<AuthUser>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    /**
     * Add a new authenticated user
     */
    fun addAuthUser(user: AuthUser) {
        val users = getAuthUsers().toMutableList()
        users.add(user)
        val json = gson.toJson(users)
        prefs.edit().putString(KEY_AUTH_USERS, json).apply()
    }

    /**
     * Update an existing authenticated user
     */
    fun updateAuthUser(updatedUser: AuthUser) {
        val users = getAuthUsers().toMutableList()
        val index = users.indexOfFirst { it.id == updatedUser.id }
        if (index != -1) {
            users[index] = updatedUser
            val json = gson.toJson(users)
            prefs.edit().putString(KEY_AUTH_USERS, json).apply()
        }
    }

    /**
     * Get current logged-in user
     */
    fun getCurrentUser(): AuthUser? {
        val json = prefs.getString(KEY_CURRENT_USER, null)
        return if (json != null) {
            gson.fromJson(json, AuthUser::class.java)
        } else {
            null
        }
    }

    /**
     * Set current logged-in user
     */
    fun setCurrentUser(user: AuthUser) {
        val json = gson.toJson(user)
        prefs.edit().putString(KEY_CURRENT_USER, json).apply()
    }

    /**
     * Logout current user
     */
    fun logout() {
        prefs.edit().remove(KEY_CURRENT_USER).apply()
    }

    /**
     * Check if user is logged in
     */
    fun isUserLoggedIn(): Boolean {
        return getCurrentUser() != null
    }
    
    /**
     * Clear all data (for testing or reset)
     */
    fun clearAllData() {
        prefs.edit().clear().apply()
    }
}

