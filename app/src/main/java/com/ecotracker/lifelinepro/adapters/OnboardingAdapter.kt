package com.ecotracker.lifelinepro.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ecotracker.lifelinepro.databinding.ItemOnboardingBinding
import com.ecotracker.lifelinepro.models.OnboardingItem

class OnboardingAdapter(
    private val onboardingItems: List<OnboardingItem>
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = ItemOnboardingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(onboardingItems[position])
    }

    override fun getItemCount(): Int = onboardingItems.size

    class OnboardingViewHolder(
        private val binding: ItemOnboardingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OnboardingItem) {
            binding.onboardingImage.setImageResource(item.image)
            binding.onboardingTitle.text = item.title
            binding.onboardingDescription.text = item.description
        }
    }
}

