package com.ecotracker.lifelinepro

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.ecotracker.lifelinepro.adapters.OnboardingAdapter
import com.ecotracker.lifelinepro.databinding.ActivityOnboardingBinding
import com.ecotracker.lifelinepro.models.OnboardingItem

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var onboardingAdapter: OnboardingAdapter
    
    private val onboardingItems by lazy {
        listOf(
            OnboardingItem(
                R.drawable.onboard01,
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_desc_1)
            ),
            OnboardingItem(
                R.drawable.onboard02,
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_desc_2)
            ),
            OnboardingItem(
                R.drawable.onboard03,
                getString(R.string.onboarding_title_3),
                getString(R.string.onboarding_desc_3)
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if onboarding has already been completed
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("onboarding_completed", false)) {
            // Skip to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupOnboardingItems()
        setupClickListeners()
    }

    private fun setupOnboardingItems() {
        onboardingAdapter = OnboardingAdapter(onboardingItems)
        binding.viewPager.adapter = onboardingAdapter

        setupIndicators()
        setCurrentIndicator(0)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
                
                // Update button text
                if (position == onboardingItems.size - 1) {
                    binding.btnNext.text = getString(R.string.onboarding_get_started)
                } else {
                    binding.btnNext.text = getString(R.string.onboarding_next)
                }
            }
        })
    }

    private fun setupIndicators() {
        val indicators = arrayOfNulls<ImageView>(onboardingItems.size)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)

        for (i in indicators.indices) {
            indicators[i] = ImageView(this)
            indicators[i]?.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.indicator_inactive
                )
            )
            indicators[i]?.layoutParams = layoutParams
            binding.dotsIndicator.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val childCount = binding.dotsIndicator.childCount
        for (i in 0 until childCount) {
            val imageView = binding.dotsIndicator.getChildAt(i) as ImageView
            if (i == index) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.indicator_inactive
                    )
                )
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem < onboardingItems.size - 1) {
                binding.viewPager.currentItem += 1
            } else {
                finishOnboarding()
            }
        }

        binding.btnSkip.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        // Save that onboarding has been completed
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_completed", true).apply()

        // Navigate to AuthActivity
        startActivity(Intent(this, com.ecotracker.lifelinepro.AuthActivity::class.java))
        finish()
    }
}

