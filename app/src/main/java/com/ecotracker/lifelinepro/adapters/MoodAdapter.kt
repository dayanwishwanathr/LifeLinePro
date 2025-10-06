package com.ecotracker.lifelinepro.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ecotracker.lifelinepro.data.models.MoodEntry
import com.ecotracker.lifelinepro.databinding.ItemMoodHistoryBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Adapter for displaying mood entries in a RecyclerView
 */
class MoodAdapter(
    private val onEditClick: (MoodEntry) -> Unit,
    private val onShareClick: (MoodEntry) -> Unit,
    private val onDeleteClick: (MoodEntry) -> Unit
) : ListAdapter<MoodEntry, MoodAdapter.MoodViewHolder>(MoodDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val binding = ItemMoodHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MoodViewHolder(private val binding: ItemMoodHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        private val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        fun bind(entry: MoodEntry) {
            binding.apply {
                moodEmoji.text = entry.emoji
                moodType.text = entry.moodType
                
                // Format time display (separate day and time)
                val dayLabel = getDayLabel(entry.timestamp)
                val time = timeFormat.format(entry.timestamp)
                
                moodTime.text = dayLabel
                timeText.text = time
                
                // Show note if exists
                if (entry.note.isNotEmpty()) {
                    moodNote.visibility = View.VISIBLE
                    moodNote.text = entry.note
                } else {
                    moodNote.visibility = View.GONE
                }
                
                // Long click to edit
                binding.root.setOnLongClickListener {
                    onEditClick(entry)
                    true
                }
                
                btnShare.setOnClickListener { onShareClick(entry) }
                btnDelete.setOnClickListener { onDeleteClick(entry) }
            }
        }

        private fun getDayLabel(timestamp: java.util.Date): String {
            return when {
                isToday(timestamp) -> "Today"
                isYesterday(timestamp) -> "Yesterday"
                else -> dateFormat.format(timestamp)
            }
        }

        private fun isToday(date: java.util.Date): Boolean {
            val today = Calendar.getInstance()
            val dateCalendar = Calendar.getInstance()
            dateCalendar.time = date
            return today.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR)
        }

        private fun isYesterday(date: java.util.Date): Boolean {
            val yesterday = Calendar.getInstance()
            yesterday.add(Calendar.DAY_OF_YEAR, -1)
            val dateCalendar = Calendar.getInstance()
            dateCalendar.time = date
            return yesterday.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                    yesterday.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR)
        }
    }

    private class MoodDiffCallback : DiffUtil.ItemCallback<MoodEntry>() {
        override fun areItemsTheSame(oldItem: MoodEntry, newItem: MoodEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MoodEntry, newItem: MoodEntry): Boolean {
            return oldItem == newItem
        }
    }
}

