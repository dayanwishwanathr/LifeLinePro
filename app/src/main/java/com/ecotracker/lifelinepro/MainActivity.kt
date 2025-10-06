package com.ecotracker.lifelinepro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.ecotracker.lifelinepro.databinding.ActivityMainBinding
import com.ecotracker.lifelinepro.ui.fragments.HomeFragment
import com.ecotracker.lifelinepro.ui.fragments.HabitsFragment
import com.ecotracker.lifelinepro.ui.fragments.HydrationFragment
import com.ecotracker.lifelinepro.ui.fragments.MoodFragment
import com.ecotracker.lifelinepro.ui.fragments.SettingsFragment

/**
 * Main activity that hosts all fragments and manages bottom navigation
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupBottomNavigation()

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // Check for notification permissions
        checkAndRequestNotificationPermission()

        // Handle intent extras (e.g., from notifications)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_habits -> HabitsFragment()
                R.id.nav_mood -> MoodFragment()
                R.id.nav_hydration -> HydrationFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> HomeFragment()
            }
            loadFragment(fragment)
            true
        }

        // Set default selected item
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.notification_permission_title)
                    .setMessage(R.string.notification_permission_message)
                    .setPositiveButton(R.string.grant_permission) { _, _ ->
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            PERMISSION_REQUEST_CODE
                        )
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    android.widget.Toast.makeText(
                        this,
                        "Notification permission granted!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    android.widget.Toast.makeText(
                        this,
                        "Notification permission denied. You won't receive reminders.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun handleIntent(intent: Intent) {
        when {
            intent.getBooleanExtra("open_hydration", false) -> {
                binding.bottomNavigation.selectedItemId = R.id.nav_hydration
                loadFragment(HydrationFragment())
                
                // Handle quick add water action from notification
                if (intent.getBooleanExtra("quick_add_water", false)) {
                    // Add 250ml water quickly
                    val prefsManager = com.ecotracker.lifelinepro.data.repository.SharedPreferencesManager.getInstance(this)
                    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val now = java.util.Date()
                    
                    val intake = com.ecotracker.lifelinepro.data.models.HydrationIntake(
                        id = java.util.UUID.randomUUID().toString(),
                        amountMl = 250,
                        timestamp = now,
                        date = dateFormat.format(now)
                    )
                    
                    prefsManager.addHydrationIntake(intake)
                    
                    android.widget.Toast.makeText(
                        this,
                        "✓ Added 250ml of water!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else -> {
                // Default behavior
            }
        }
    }
}
