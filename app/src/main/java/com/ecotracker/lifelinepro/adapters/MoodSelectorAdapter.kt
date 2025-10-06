package com.ecotracker.lifelinepro.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ecotracker.lifelinepro.databinding.ItemMoodSelectorBinding

/**
 * Adapter for mood selector in dialog
 */
class MoodSelectorAdapter(
    private val moods: List<MoodItem>,
    private val onMoodSelected: (MoodItem) -> Unit
) : RecyclerView.Adapter<MoodSelectorAdapter.MoodSelectorViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodSelectorViewHolder {
        val binding = ItemMoodSelectorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MoodSelectorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoodSelectorViewHolder, position: Int) {
        holder.bind(moods[position], position == selectedPosition)
    }

    override fun getItemCount() = moods.size

    inner class MoodSelectorViewHolder(private val binding: ItemMoodSelectorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(mood: MoodItem, isSelected: Boolean) {
            binding.apply {
                emojiText.text = mood.emoji
                moodLabel.text = mood.label

                // Highlight selected mood
                if (isSelected) {
                    emojiText.setBackgroundResource(com.ecotracker.lifelinepro.R.drawable.mood_selector_background)
                    emojiText.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        root.context.getColor(com.ecotracker.lifelinepro.R.color.primary_light)
                    )
                } else {
                    emojiText.setBackgroundResource(com.ecotracker.lifelinepro.R.drawable.mood_selector_background)
                    emojiText.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        root.context.getColor(com.ecotracker.lifelinepro.R.color.surface_variant)
                    )
                }

                root.setOnClickListener {
                    val previousPosition = selectedPosition
                    selectedPosition = bindingAdapterPosition
                    notifyItemChanged(previousPosition)
                    notifyItemChanged(selectedPosition)
                    onMoodSelected(mood)
                }
            }
        }
    }

    data class MoodItem(
        val emoji: String,
        val label: String,
        val moodType: String
    )
}

