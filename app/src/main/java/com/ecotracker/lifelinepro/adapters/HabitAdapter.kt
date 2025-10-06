package com.ecotracker.lifelinepro.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ecotracker.lifelinepro.R
import com.ecotracker.lifelinepro.data.models.Habit
import com.ecotracker.lifelinepro.databinding.ItemHabitCleanBinding

/**
 * Adapter for displaying habits in a RecyclerView
 */
class HabitAdapter(
    private val onIncrementClick: (Habit) -> Unit,
    private val onEditClick: (Habit) -> Unit,
    private val onDeleteClick: (Habit) -> Unit
) : ListAdapter<Habit, HabitAdapter.HabitViewHolder>(HabitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitCleanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HabitViewHolder(private val binding: ItemHabitCleanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: Habit) {
            binding.apply {
                // Set habit details
                habitName.text = habit.name
                habitCategory.text = habit.category
                targetText.text = "Target: ${habit.targetCount} ${habit.unit}"
                
                // Set progress
                val percentage = habit.getCompletionPercentage()
                progressText.text = "Progress: ${habit.currentCount}/${habit.targetCount} ${habit.unit}"
                habitProgressBar.progress = percentage
                
                // Set badge
                badgeText.text = habit.currentCount.toString()
                
                // Change badge color based on completion
                if (habit.isCompleted) {
                    progressBadge.setCardBackgroundColor(itemView.context.getColor(R.color.success))
                    btnMarkComplete.text = "Completed ✓"
                    btnMarkComplete.isEnabled = false
                } else {
                    progressBadge.setCardBackgroundColor(itemView.context.getColor(R.color.accent))
                    btnMarkComplete.text = "Mark Complete"
                    btnMarkComplete.isEnabled = true
                }
                
                // Button click listeners
                btnMarkComplete.setOnClickListener { 
                    if (!habit.isCompleted) {
                        onIncrementClick(habit)
                    }
                }
                
                btnEdit.setOnClickListener { onEditClick(habit) }
                btnDelete.setOnClickListener { onDeleteClick(habit) }
                
                // Share button - share habit progress
                btnShare.setOnClickListener {
                    val context = itemView.context
                    val shareText = """
                        🎯 My Habit: ${habit.name}
                        📊 Progress: ${habit.currentCount}/${habit.targetCount} ${habit.unit}
                        📈 ${percentage}% Complete
                        
                        Track your habits with LifeLinePro!
                    """.trimIndent()
                    
                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Habit"))
                }
            }
        }
    }

    private class HabitDiffCallback : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem == newItem
        }
    }
}

