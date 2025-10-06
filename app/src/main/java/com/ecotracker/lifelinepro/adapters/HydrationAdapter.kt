package com.ecotracker.lifelinepro.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ecotracker.lifelinepro.data.models.HydrationIntake
import com.ecotracker.lifelinepro.databinding.ItemHydrationIntakeBinding
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adapter for displaying hydration intake history
 */
class HydrationAdapter(
    private val onDeleteClick: (HydrationIntake) -> Unit
) : ListAdapter<HydrationIntake, HydrationAdapter.HydrationViewHolder>(HydrationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HydrationViewHolder {
        val binding = ItemHydrationIntakeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HydrationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HydrationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HydrationViewHolder(private val binding: ItemHydrationIntakeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        fun bind(intake: HydrationIntake) {
            binding.apply {
                amountText.text = "${intake.amountMl} ml"
                timeText.text = timeFormat.format(intake.timestamp)
                
                btnDelete.setOnClickListener { onDeleteClick(intake) }
            }
        }
    }

    private class HydrationDiffCallback : DiffUtil.ItemCallback<HydrationIntake>() {
        override fun areItemsTheSame(oldItem: HydrationIntake, newItem: HydrationIntake): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HydrationIntake, newItem: HydrationIntake): Boolean {
            return oldItem == newItem
        }
    }
}

