package com.ecotracker.lifelinepro.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecotracker.lifelinepro.R
import com.ecotracker.lifelinepro.adapters.HabitAdapter
import com.ecotracker.lifelinepro.data.models.Habit
import com.ecotracker.lifelinepro.data.repository.SharedPreferencesManager
import com.ecotracker.lifelinepro.databinding.DialogAddHabitBinding
import com.ecotracker.lifelinepro.databinding.FragmentHabitsBinding
import java.util.Date
import java.util.UUID

/**
 * Fragment for displaying and managing daily habits
 */
class HabitsFragment : Fragment() {

    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefsManager: SharedPreferencesManager
    private lateinit var habitAdapter: HabitAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsManager = SharedPreferencesManager.getInstance(requireContext())
        
        setupRecyclerView()
        setupFab()
        loadHabits()
    }

    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(
            onIncrementClick = { habit -> incrementHabit(habit) },
            onEditClick = { habit -> showEditHabitDialog(habit) },
            onDeleteClick = { habit -> showDeleteConfirmation(habit) }
        )

        binding.habitsRecyclerView.apply {
            // Use GridLayoutManager for tablets in portrait mode
            val isTablet = resources.getBoolean(R.bool.isTablet)
            layoutManager = if (isTablet) {
                androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2)
            } else {
                LinearLayoutManager(requireContext())
            }
            adapter = habitAdapter
        }
    }

    private fun setupFab() {
        binding.fabAddHabit.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun loadHabits() {
        val habits = prefsManager.getHabits()
        habitAdapter.submitList(habits)
        
        // Update completion statistics
        val completionPercentage = if (habits.isNotEmpty()) {
            prefsManager.getTodayHabitCompletionPercentage()
        } else {
            0
        }
        
        val completedCount = prefsManager.getCompletedHabitsCount()
        val totalHabits = habits.size
        
        // Update UI with animation
        binding.percentageText?.text = "$completionPercentage%"
        binding.circularProgress?.progress = completionPercentage
        binding.completionText?.text = "$completedCount of $totalHabits completed"
        
        // Update encouragement message based on progress
        binding.encouragementText?.text = when {
            completionPercentage == 100 -> "Perfect! 🎉"
            completionPercentage >= 75 -> "Almost there! 💪"
            completionPercentage >= 50 -> "Keep it up! 👍"
            completionPercentage > 0 -> "Good start! 🌟"
            else -> "Let's begin! ✨"
        }
        
        // Animate progress change
        binding.circularProgress?.let {
            android.animation.ObjectAnimator.ofInt(it, "progress", 0, completionPercentage)
                .setDuration(800)
                .start()
        }
        
        // Show/hide empty state
        if (habits.isEmpty()) {
            binding.emptyState?.visibility = View.VISIBLE
            binding.habitsRecyclerView.visibility = View.GONE
            binding.progressCard?.visibility = View.GONE
            binding.habitsTitle?.visibility = View.GONE
        } else {
            binding.emptyState?.visibility = View.GONE
            binding.habitsRecyclerView.visibility = View.VISIBLE
            binding.progressCard?.visibility = View.VISIBLE
            binding.habitsTitle?.visibility = View.VISIBLE
        }
        
        // Update widget when habits change
        com.ecotracker.lifelinepro.widget.HabitProgressWidget.updateAllWidgets(requireContext())
    }

    private fun showAddHabitDialog() {
        val dialogBinding = DialogAddHabitBinding.inflate(layoutInflater)
        
        // Setup unit dropdown
        val units = arrayOf("times", "glasses", "hours", "minutes", "servings", "pages", "steps")
        val unitAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, units)
        dialogBinding.habitUnitInput.setAdapter(unitAdapter)
        
        // Setup category dropdown
        val categories = arrayOf("Personal", "Health", "Fitness", "Work", "Learning", "Social", "Other")
        val categoryAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        dialogBinding.habitCategoryInput.setAdapter(categoryAdapter)
        
        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
            .apply {
                dialogBinding.btnCancel.setOnClickListener { dismiss() }
                dialogBinding.btnSave.setOnClickListener {
                    val name = dialogBinding.habitNameInput.text.toString().trim()
                    val description = dialogBinding.habitDescriptionInput.text.toString().trim()
                    val targetStr = dialogBinding.habitTargetInput.text.toString().trim()
                    val unit = dialogBinding.habitUnitInput.text.toString().trim()
                    val category = dialogBinding.habitCategoryInput.text.toString().trim()
                    
                    if (name.isEmpty()) {
                        dialogBinding.habitNameInputLayout.error = "Please enter habit name"
                        return@setOnClickListener
                    }
                    
                    val target = targetStr.toIntOrNull() ?: 1
                    if (target <= 0) {
                        dialogBinding.habitTargetInputLayout.error = "Target must be greater than 0"
                        return@setOnClickListener
                    }
                    
                    val habit = Habit(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        description = description,
                        icon = "ic_habits",
                        targetCount = target,
                        currentCount = 0,
                        unit = unit,
                        category = category,
                        lastUpdated = Date(),
                        isCompleted = false
                    )
                    
                    prefsManager.addHabit(habit)
                    loadHabits()
                    dismiss()
                }
                show()
            }
    }

    private fun showEditHabitDialog(habit: Habit) {
        val dialogBinding = DialogAddHabitBinding.inflate(layoutInflater)
        dialogBinding.dialogTitle.text = getString(R.string.edit_habit)
        dialogBinding.habitNameInput.setText(habit.name)
        dialogBinding.habitDescriptionInput.setText(habit.description)
        dialogBinding.habitTargetInput.setText(habit.targetCount.toString())
        dialogBinding.habitUnitInput.setText(habit.unit)
        dialogBinding.habitCategoryInput.setText(habit.category)
        
        // Setup unit dropdown
        val units = arrayOf("times", "glasses", "hours", "minutes", "servings", "pages", "steps")
        val unitAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, units)
        dialogBinding.habitUnitInput.setAdapter(unitAdapter)
        
        // Setup category dropdown
        val categories = arrayOf("Personal", "Health", "Fitness", "Work", "Learning", "Social", "Other")
        val categoryAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        dialogBinding.habitCategoryInput.setAdapter(categoryAdapter)
        
        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
            .apply {
                dialogBinding.btnCancel.setOnClickListener { dismiss() }
                dialogBinding.btnSave.setOnClickListener {
                    val name = dialogBinding.habitNameInput.text.toString().trim()
                    val description = dialogBinding.habitDescriptionInput.text.toString().trim()
                    val targetStr = dialogBinding.habitTargetInput.text.toString().trim()
                    val unit = dialogBinding.habitUnitInput.text.toString().trim()
                    val category = dialogBinding.habitCategoryInput.text.toString().trim()
                    
                    if (name.isEmpty()) {
                        dialogBinding.habitNameInputLayout.error = "Please enter habit name"
                        return@setOnClickListener
                    }
                    
                    val target = targetStr.toIntOrNull() ?: 1
                    if (target <= 0) {
                        dialogBinding.habitTargetInputLayout.error = "Target must be greater than 0"
                        return@setOnClickListener
                    }
                    
                    val updatedHabit = habit.copy(
                        name = name,
                        description = description,
                        targetCount = target,
                        unit = unit,
                        category = category,
                        isCompleted = habit.currentCount >= target
                    )
                    
                    prefsManager.updateHabit(updatedHabit)
                    loadHabits()
                    dismiss()
                }
                show()
            }
    }

    private fun incrementHabit(habit: Habit) {
        val newCount = (habit.currentCount + 1).coerceAtMost(habit.targetCount)
        val updatedHabit = habit.copy(
            currentCount = newCount,
            isCompleted = newCount >= habit.targetCount,
            lastUpdated = Date()
        )
        
        prefsManager.updateHabit(updatedHabit)
        loadHabits()
        
        if (updatedHabit.isCompleted && !habit.isCompleted) {
            // Show celebration with custom toast
            val toast = android.widget.Toast.makeText(
                requireContext(),
                "🎉 ${habit.name} completed! Well done!",
                android.widget.Toast.LENGTH_LONG
            )
            toast.show()
            
            // Animate the card
            binding.progressCard?.animate()
                ?.scaleX(1.05f)
                ?.scaleY(1.05f)
                ?.setDuration(200)
                ?.withEndAction {
                    binding.progressCard?.animate()
                        ?.scaleX(1f)
                        ?.scaleY(1f)
                        ?.setDuration(200)
                        ?.start()
                }
                ?.start()
        }
    }

    private fun showDeleteConfirmation(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_habit)
            .setMessage(R.string.delete_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                prefsManager.deleteHabit(habit.id)
                loadHabits()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

