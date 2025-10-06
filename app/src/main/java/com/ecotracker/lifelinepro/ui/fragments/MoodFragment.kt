package com.ecotracker.lifelinepro.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecotracker.lifelinepro.R
import com.ecotracker.lifelinepro.adapters.MoodAdapter
import com.ecotracker.lifelinepro.adapters.MoodSelectorAdapter
import com.ecotracker.lifelinepro.data.models.MoodEntry
import com.ecotracker.lifelinepro.data.repository.SharedPreferencesManager
import com.ecotracker.lifelinepro.databinding.DialogMoodSelectorBinding
import com.ecotracker.lifelinepro.databinding.FragmentMoodBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Fragment for mood journal with emoji selector and mood tracking
 */
class MoodFragment : Fragment() {

    private var _binding: FragmentMoodBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefsManager: SharedPreferencesManager
    private lateinit var moodAdapter: MoodAdapter
    private lateinit var selectedDateMoodAdapter: MoodAdapter
    private var selectedMood: MoodSelectorAdapter.MoodItem? = null
    private var isCalendarView = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsManager = SharedPreferencesManager.getInstance(requireContext())
        
        setupRecyclerView()
        setupFab()
        setupViewTrendsButton()
        setupViewToggle()
        setupCalendarView()
        loadMoodEntries()
    }

    private fun setupRecyclerView() {
        moodAdapter = MoodAdapter(
            onEditClick = { entry -> showEditMoodDialog(entry) },
            onShareClick = { entry -> shareMood(entry) },
            onDeleteClick = { entry -> showDeleteConfirmation(entry) }
        )

        binding.moodRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = moodAdapter
        }

        // Setup adapter for selected date in calendar view
        selectedDateMoodAdapter = MoodAdapter(
            onEditClick = { entry -> showEditMoodDialog(entry) },
            onShareClick = { entry -> shareMood(entry) },
            onDeleteClick = { entry -> showDeleteConfirmation(entry) }
        )

        binding.selectedDateMoodsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = selectedDateMoodAdapter
        }

    }

    private fun setupViewToggle() {
        binding.btnListView.setOnClickListener {
            switchToListView()
        }

        binding.btnCalendarView.setOnClickListener {
            switchToCalendarView()
        }
    }

    private fun switchToListView() {
        isCalendarView = false
        binding.moodRecyclerView.visibility = View.VISIBLE
        binding.moodCalendarView.visibility = View.GONE
        binding.selectedDateMoodsRecyclerView.visibility = View.GONE
        binding.historyTitle.text = "Recent Entries"

        // Update button styles
        binding.btnListView.apply {
            strokeWidth = 0
            setTextColor(requireContext().getColor(R.color.white))
            backgroundTintList = android.content.res.ColorStateList.valueOf(
                requireContext().getColor(R.color.primary)
            )
        }
        binding.btnCalendarView.apply {
            strokeWidth = 2
            setTextColor(requireContext().getColor(R.color.primary))
            backgroundTintList = null
        }
    }

    private fun switchToCalendarView() {
        isCalendarView = true
        binding.moodRecyclerView.visibility = View.GONE
        binding.moodCalendarView.visibility = View.VISIBLE
        binding.selectedDateMoodsRecyclerView.visibility = View.VISIBLE
        binding.historyTitle.text = "Calendar View"

        // Update button styles
        binding.btnCalendarView.apply {
            strokeWidth = 0
            setTextColor(requireContext().getColor(R.color.white))
            backgroundTintList = android.content.res.ColorStateList.valueOf(
                requireContext().getColor(R.color.primary)
            )
        }
        binding.btnListView.apply {
            strokeWidth = 2
            setTextColor(requireContext().getColor(R.color.primary))
            backgroundTintList = null
        }

        // Load today's moods initially
        loadMoodsForSelectedDate(binding.moodCalendarView.date)
    }

    private fun setupCalendarView() {
        binding.moodCalendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = java.util.Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            loadMoodsForSelectedDate(calendar.timeInMillis)
        }
        
        // Set initial date to today
        binding.moodCalendarView.date = System.currentTimeMillis()
    }

    private fun loadMoodsForSelectedDate(dateInMillis: Long) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDate = dateFormat.format(Date(dateInMillis))

        val allEntries = prefsManager.getMoodEntries()
        val filteredEntries = allEntries.filter { it.date == selectedDate }
            .sortedByDescending { it.timestamp }

        selectedDateMoodAdapter.submitList(filteredEntries)
    }

    private fun setupFab() {
        binding.fabLogMood.setOnClickListener {
            showMoodSelectorDialog()
        }
        
        binding.btnAddFirstMood?.setOnClickListener {
            showMoodSelectorDialog()
        }
    }

    private fun setupViewTrendsButton() {
        binding.btnViewStatistics.setOnClickListener {
            showMoodStatisticsDialog()
        }
    }

    private fun loadMoodEntries(scrollToTop: Boolean = false) {
        val entries = prefsManager.getMoodEntries()
        
        // Sort entries by timestamp (newest first)
        val sortedEntries = entries.sortedByDescending { it.timestamp }
        
        moodAdapter.submitList(sortedEntries) {
            // Scroll to top after list is updated (if requested)
            if (scrollToTop && sortedEntries.isNotEmpty()) {
                binding.moodRecyclerView.smoothScrollToPosition(0)
            }
        }
        
        
        // Update statistics
        updateStatistics(entries)
        
        // Show/hide empty state and stats card
        if (entries.isEmpty()) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.moodRecyclerView.visibility = View.GONE
            binding.statsCard?.visibility = View.GONE
            binding.historyTitle?.visibility = View.GONE
        } else {
            binding.emptyStateLayout.visibility = View.GONE
            binding.moodRecyclerView.visibility = View.VISIBLE
            binding.statsCard?.visibility = View.VISIBLE
            binding.historyTitle?.visibility = View.VISIBLE
        }
    }
    
    
    private fun updateStatistics(entries: List<MoodEntry>) {
        if (entries.isEmpty()) return
        
        // Calculate average mood
        val avgValue = entries.map { MoodEntry.moodToValue(it.moodType) }.average().toFloat()
        val avgEmoji = when {
            avgValue >= 4.5f -> "😄"
            avgValue >= 3.5f -> "😊"
            avgValue >= 2.5f -> "😐"
            avgValue >= 1.5f -> "😔"
            else -> "😢"
        }
        binding.avgMoodEmoji?.text = avgEmoji
        
        // Total entries
        binding.totalEntriesText?.text = entries.size.toString()
        
        // Calculate streak (consecutive days with mood entries)
        val streak = calculateStreak(entries)
        binding.streakText?.text = streak.toString()
    }
    
    private fun calculateStreak(entries: List<MoodEntry>): Int {
        if (entries.isEmpty()) return 0
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        
        // Get unique dates
        val uniqueDates = entries.map { it.date }.distinct().sortedDescending()
        
        // Check if today has entry
        if (uniqueDates.isEmpty() || uniqueDates[0] != today) return 0
        
        var streak = 1
        val calendar = java.util.Calendar.getInstance()
        
        for (i in 0 until uniqueDates.size - 1) {
            calendar.time = dateFormat.parse(uniqueDates[i])!!
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
            val expectedDate = dateFormat.format(calendar.time)
            
            if (uniqueDates[i + 1] == expectedDate) {
                streak++
            } else {
                break
            }
        }
        
        return streak
    }

    private fun showMoodSelectorDialog() {
        val dialogBinding = DialogMoodSelectorBinding.inflate(layoutInflater)
        
        // Define mood options
        val moods = listOf(
            MoodSelectorAdapter.MoodItem("😄", "Very Happy", MoodEntry.MOOD_VERY_HAPPY),
            MoodSelectorAdapter.MoodItem("😊", "Happy", MoodEntry.MOOD_HAPPY),
            MoodSelectorAdapter.MoodItem("😐", "Neutral", MoodEntry.MOOD_NEUTRAL),
            MoodSelectorAdapter.MoodItem("😔", "Sad", MoodEntry.MOOD_SAD),
            MoodSelectorAdapter.MoodItem("😢", "Very Sad", MoodEntry.MOOD_VERY_SAD)
        )
        
        val moodSelectorAdapter = MoodSelectorAdapter(moods) { mood ->
            selectedMood = mood
        }
        
        dialogBinding.moodRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 5)
            adapter = moodSelectorAdapter
        }
        
        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
            .apply {
                dialogBinding.btnCancel.setOnClickListener { dismiss() }
                dialogBinding.btnSave.setOnClickListener {
                    if (selectedMood == null) {
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Please select a mood",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    
                    val note = dialogBinding.noteInput.text.toString().trim()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val now = Date()
                    
                    val entry = MoodEntry(
                        id = UUID.randomUUID().toString(),
                        emoji = selectedMood!!.emoji,
                        moodType = selectedMood!!.moodType,
                        note = note,
                        timestamp = now,
                        date = dateFormat.format(now)
                    )
                    
                    prefsManager.addMoodEntry(entry)
                    loadMoodEntries(scrollToTop = true)
                    
                    // Refresh calendar view if it's currently active
                    if (isCalendarView) {
                        loadMoodsForSelectedDate(binding.moodCalendarView.date)
                    }
                    
                    android.widget.Toast.makeText(
                        requireContext(),
                        "✅ Mood logged successfully!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    
                    selectedMood = null
                    dismiss()
                }
                show()
            }
    }

    private fun showMoodStatisticsDialog() {
        val entries = prefsManager.getMoodEntries()
        
        if (entries.isEmpty()) {
            android.widget.Toast.makeText(
                requireContext(),
                "No mood data available",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        // Create statistics dialog
        val dialogView = layoutInflater.inflate(R.layout.dialog_mood_statistics, null)
        
        // Calculate statistics
        val totalEntries = entries.size
        val avgValue = entries.map { MoodEntry.moodToValue(it.moodType) }.average().toFloat()
        val avgEmoji = when {
            avgValue >= 4.5f -> "😄"
            avgValue >= 3.5f -> "😊"
            avgValue >= 2.5f -> "😐"
            avgValue >= 1.5f -> "😔"
            else -> "😢"
        }
        val streak = calculateStreak(entries)
        
        // Count moods
        val veryHappyCount = entries.count { it.moodType == MoodEntry.MOOD_VERY_HAPPY }
        val happyCount = entries.count { it.moodType == MoodEntry.MOOD_HAPPY }
        val neutralCount = entries.count { it.moodType == MoodEntry.MOOD_NEUTRAL }
        val sadCount = entries.count { it.moodType == MoodEntry.MOOD_SAD }
        val verySadCount = entries.count { it.moodType == MoodEntry.MOOD_VERY_SAD }
        
        // Set values
        dialogView.findViewById<android.widget.TextView>(R.id.totalEntriesValue).text = totalEntries.toString()
        dialogView.findViewById<android.widget.TextView>(R.id.averageMoodEmoji).text = avgEmoji
        dialogView.findViewById<android.widget.TextView>(R.id.streakValue).text = streak.toString()
        
        dialogView.findViewById<android.widget.TextView>(R.id.veryHappyCount).text = veryHappyCount.toString()
        dialogView.findViewById<android.widget.TextView>(R.id.happyCount).text = happyCount.toString()
        dialogView.findViewById<android.widget.TextView>(R.id.neutralCount).text = neutralCount.toString()
        dialogView.findViewById<android.widget.TextView>(R.id.sadCount).text = sadCount.toString()
        dialogView.findViewById<android.widget.TextView>(R.id.verySadCount).text = verySadCount.toString()
        
        // Setup chart
        val chartContainer = dialogView.findViewById<android.widget.FrameLayout>(R.id.chartContainer)
        val lastWeekEntries = prefsManager.getMoodEntriesForLastWeek()
        
        if (lastWeekEntries.isNotEmpty()) {
            val chartView = com.github.mikephil.charting.charts.LineChart(requireContext())
            setupMoodChart(chartView, lastWeekEntries)
            chartContainer.addView(chartView)
        } else {
            val emptyText = android.widget.TextView(requireContext()).apply {
                text = "Not enough data for trend chart"
                textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
                setTextColor(requireContext().getColor(R.color.text_secondary))
                gravity = android.view.Gravity.CENTER
            }
            chartContainer.addView(emptyText)
        }
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnClose)
            .setOnClickListener { dialog.dismiss() }
        
        dialog.show()
    }

    private fun setupMoodChart(chart: com.github.mikephil.charting.charts.LineChart, entries: List<MoodEntry>) {
        // Group entries by date and calculate daily averages
        val dailyAverages = mutableMapOf<String, Float>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        // Group entries by date
        val entriesByDate = entries.groupBy { dateFormat.format(it.timestamp) }
        
        // Calculate daily averages
        entriesByDate.forEach { (date, dayEntries) ->
            val averageValue = dayEntries.map { MoodEntry.moodToValue(it.moodType) }.average().toFloat()
            dailyAverages[date] = averageValue
        }
        
        // Create chart entries for the last 7 days
        val calendar = Calendar.getInstance()
        val chartEntries = mutableListOf<Entry>()
        val xLabels = mutableListOf<String>()
        
        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dateString = dateFormat.format(calendar.time)
            val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
            
            val moodValue = dailyAverages[dateString] ?: 0f
            chartEntries.add(Entry((6 - i).toFloat(), moodValue))
            xLabels.add(dayName)
        }
        
        val dataSet = LineDataSet(chartEntries, "Daily Average Mood").apply {
            color = requireContext().getColor(R.color.primary)
            valueTextColor = requireContext().getColor(R.color.text_primary)
            lineWidth = 3f
            circleRadius = 6f
            setCircleColor(requireContext().getColor(R.color.primary))
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = requireContext().getColor(R.color.primary_light)
            fillAlpha = 50
        }
        
        val lineData = LineData(dataSet)
        
        chart.apply {
            data = lineData
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(true)
                granularity = 1f
                setDrawAxisLine(true)
                textColor = requireContext().getColor(R.color.text_secondary)
                textSize = 12f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index >= 0 && index < xLabels.size) {
                            xLabels[index]
                        } else ""
                    }
                }
            }
            
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 6f
                granularity = 1f
                setDrawGridLines(true)
                textColor = requireContext().getColor(R.color.text_secondary)
                textSize = 12f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when (value.toInt()) {
                            1 -> "😢"
                            2 -> "😔"
                            3 -> "😐"
                            4 -> "😊"
                            5 -> "😄"
                            else -> ""
                        }
                    }
                }
            }
            
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(false)
            
            // Set background
            setBackgroundColor(requireContext().getColor(R.color.surface))
            
            invalidate()
        }
    }

    private fun showEditMoodDialog(entry: MoodEntry) {
        val dialogBinding = DialogMoodSelectorBinding.inflate(layoutInflater)
        
        // Pre-select the current mood
        selectedMood = MoodSelectorAdapter.MoodItem(entry.emoji, entry.moodType, entry.moodType)
        dialogBinding.noteInput.setText(entry.note)
        
        // Define mood options
        val moods = listOf(
            MoodSelectorAdapter.MoodItem("😄", "Very Happy", MoodEntry.MOOD_VERY_HAPPY),
            MoodSelectorAdapter.MoodItem("😊", "Happy", MoodEntry.MOOD_HAPPY),
            MoodSelectorAdapter.MoodItem("😐", "Neutral", MoodEntry.MOOD_NEUTRAL),
            MoodSelectorAdapter.MoodItem("😔", "Sad", MoodEntry.MOOD_SAD),
            MoodSelectorAdapter.MoodItem("😢", "Very Sad", MoodEntry.MOOD_VERY_SAD)
        )
        
        val moodSelectorAdapter = MoodSelectorAdapter(moods) { mood ->
            selectedMood = mood
        }
        
        dialogBinding.moodRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 5)
            adapter = moodSelectorAdapter
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Update Mood")
            .setView(dialogBinding.root)
            .create()
            .apply {
                dialogBinding.btnCancel.setOnClickListener { dismiss() }
                dialogBinding.btnSave.setOnClickListener {
                    if (selectedMood == null) {
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Please select a mood",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    
                    val note = dialogBinding.noteInput.text.toString().trim()
                    
                    val updatedEntry = entry.copy(
                        emoji = selectedMood!!.emoji,
                        moodType = selectedMood!!.moodType,
                        note = note
                    )
                    
                    prefsManager.updateMoodEntry(updatedEntry)
                    loadMoodEntries(scrollToTop = false)
                    
                    // Refresh calendar view if it's currently active
                    if (isCalendarView) {
                        loadMoodsForSelectedDate(binding.moodCalendarView.date)
                    }
                    
                    android.widget.Toast.makeText(
                        requireContext(),
                        "✅ Mood updated successfully!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    
                    selectedMood = null
                    dismiss()
                }
                show()
            }
    }
    
    private fun shareMood(entry: MoodEntry) {
        val timeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        val shareText = buildString {
            append("😊 My Mood: ${entry.moodType} ${entry.emoji}\n")
            append("📅 ${timeFormat.format(entry.timestamp)}\n")
            if (entry.note.isNotEmpty()) {
                append("💭 Note: ${entry.note}\n")
            }
            append("\nTrack your moods with LifeLinePro!")
        }
        
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Share Mood"))
    }

    private fun showDeleteConfirmation(entry: MoodEntry) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete)
            .setMessage(R.string.delete_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                prefsManager.deleteMoodEntry(entry.id)
                loadMoodEntries()
                
                // Refresh calendar view if it's currently active
                if (isCalendarView) {
                    loadMoodsForSelectedDate(binding.moodCalendarView.date)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

