package com.ecotracker.lifelinepro.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecotracker.lifelinepro.R
import com.ecotracker.lifelinepro.adapters.HydrationAdapter
import com.ecotracker.lifelinepro.data.models.HydrationIntake
import com.ecotracker.lifelinepro.data.repository.SharedPreferencesManager
import com.ecotracker.lifelinepro.databinding.DialogAddWaterBinding
import com.ecotracker.lifelinepro.databinding.FragmentHydrationBinding
import com.ecotracker.lifelinepro.widget.HabitProgressWidget
import com.ecotracker.lifelinepro.utils.HydrationReminderScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Fragment for tracking water intake and managing hydration reminders
 */
class HydrationFragment : Fragment() {

    private var _binding: FragmentHydrationBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefsManager: SharedPreferencesManager
    private lateinit var hydrationAdapter: HydrationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHydrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsManager = SharedPreferencesManager.getInstance(requireContext())
        
        setupRecyclerView()
        setupFab()
        setupReminderSettings()
        loadHydrationData()
    }

    private fun setupRecyclerView() {
        hydrationAdapter = HydrationAdapter(
            onDeleteClick = { intake -> showDeleteConfirmation(intake) }
        )

        binding.hydrationRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = hydrationAdapter
        }
    }

    private fun setupFab() {
        binding.fabAddWater.setOnClickListener {
            showAddWaterDialog()
        }
    }

    private fun setupReminderSettings() {
        val settings = prefsManager.getUserSettings()
        
        binding.reminderSwitch.isChecked = settings.reminderEnabled
        binding.intervalSeekBar.progress = settings.reminderIntervalMinutes
        binding.intervalText.text = "${settings.reminderIntervalMinutes} min"
        
        // Update interval when enabled/disabled
        binding.reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            val updatedSettings = settings.copy(reminderEnabled = isChecked)
            prefsManager.saveUserSettings(updatedSettings)
            
            if (isChecked) {
                HydrationReminderScheduler.scheduleReminder(
                    requireContext(),
                    settings.reminderIntervalMinutes
                )
                android.widget.Toast.makeText(
                    requireContext(),
                    "Hydration reminders enabled",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } else {
                HydrationReminderScheduler.cancelReminder(requireContext())
                android.widget.Toast.makeText(
                    requireContext(),
                    "Hydration reminders disabled",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
        
        // Update interval text as seekbar changes
        binding.intervalSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.intervalText.text = "$progress min"
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val interval = seekBar?.progress ?: 60
                val updatedSettings = settings.copy(reminderIntervalMinutes = interval)
                prefsManager.saveUserSettings(updatedSettings)
                
                if (settings.reminderEnabled) {
                    HydrationReminderScheduler.scheduleReminder(requireContext(), interval)
                }
            }
        })
    }

    private fun loadHydrationData() {
        val settings = prefsManager.getUserSettings()
        val todayIntake = prefsManager.getTodayWaterIntake()
        val goal = settings.dailyWaterGoalMl
        
        // Update UI
        binding.waterConsumedText.text = "$todayIntake ml"
        binding.waterGoalText.text = "of $goal ml goal"
        
        val percentage = ((todayIntake.toFloat() / goal) * 100).toInt().coerceAtMost(100)
        binding.waterProgressBar.progress = percentage
        binding.percentageText?.text = "$percentage%"
        
        // Animate progress
        android.animation.ObjectAnimator.ofInt(binding.waterProgressBar, "progress", 0, percentage)
            .setDuration(600)
            .start()
        
        // Load today's intake history (newest first)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        val todayIntakes = prefsManager.getHydrationIntakes()
            .filter { it.date == today }
            .sortedByDescending { it.timestamp }
        
        // Submit null first, then the new list to ensure proper update
        hydrationAdapter.submitList(null)
        hydrationAdapter.submitList(todayIntakes)
    }

    private fun showAddWaterDialog() {
        val dialogBinding = DialogAddWaterBinding.inflate(layoutInflater)
        
        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
            .apply {
                // Quick add buttons
                dialogBinding.btn100ml.setOnClickListener {
                    addWater(100)
                    dismiss()
                }
                
                dialogBinding.btn250ml.setOnClickListener {
                    addWater(250)
                    dismiss()
                }
                
                dialogBinding.btn500ml.setOnClickListener {
                    addWater(500)
                    dismiss()
                }
                
                dialogBinding.btnCancel.setOnClickListener { dismiss() }
                
                dialogBinding.btnAdd.setOnClickListener {
                    val amountStr = dialogBinding.waterAmountInput.text.toString().trim()
                    
                    if (amountStr.isEmpty()) {
                        dialogBinding.waterAmountInputLayout.error = "Please enter amount"
                        return@setOnClickListener
                    }
                    
                    val amount = amountStr.toIntOrNull()
                    if (amount == null || amount <= 0) {
                        dialogBinding.waterAmountInputLayout.error = "Please enter valid amount"
                        return@setOnClickListener
                    }
                    
                    addWater(amount)
                    dismiss()
                }
                
                show()
            }
    }

    private fun addWater(amountMl: Int) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val now = Date()
        
        val intake = HydrationIntake(
            id = UUID.randomUUID().toString(),
            amountMl = amountMl,
            timestamp = now,
            date = dateFormat.format(now)
        )
        
        prefsManager.addHydrationIntake(intake)
        loadHydrationData()
        
        // Update widget
        HabitProgressWidget.updateAllWidgets(requireContext())
        
        android.widget.Toast.makeText(
            requireContext(),
            R.string.water_added,
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    private fun showDeleteConfirmation(intake: HydrationIntake) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete)
            .setMessage(R.string.delete_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                prefsManager.deleteHydrationIntake(intake.id)
                loadHydrationData()
                // Update widget
                HabitProgressWidget.updateAllWidgets(requireContext())
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

