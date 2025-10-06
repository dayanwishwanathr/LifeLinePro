package com.ecotracker.lifelinepro.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecotracker.lifelinepro.R
import com.ecotracker.lifelinepro.data.models.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

class MoodCalendarAdapter(
    private val onDateClick: (Long) -> Unit
) : RecyclerView.Adapter<MoodCalendarAdapter.CalendarViewHolder>() {

    private var currentMonth = Calendar.getInstance()
    private var moodEntries: List<MoodEntry> = emptyList()
    private var selectedDate: Long = System.currentTimeMillis()

    fun updateMoodEntries(entries: List<MoodEntry>) {
        moodEntries = entries
        notifyDataSetChanged()
    }

    fun setSelectedDate(date: Long) {
        selectedDate = date
        notifyDataSetChanged()
    }

    fun setCurrentMonth(month: Int, year: Int) {
        currentMonth.set(year, month, 1)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_date, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val calendar = Calendar.getInstance()
        calendar.time = currentMonth.time
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        
        // Calculate the first day of the month and adjust for the grid
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // Calculate the actual day for this position
        val dayOfMonth = position - firstDayOfWeek + 2
        
        if (dayOfMonth < 1 || dayOfMonth > daysInMonth) {
            // Empty cell
            holder.bindEmpty()
        } else {
            // Valid day
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val dateInMillis = calendar.timeInMillis
            val moodForDate = getMoodForDate(dateInMillis)
            val isSelected = isSameDay(dateInMillis, selectedDate)
            val isToday = isSameDay(dateInMillis, System.currentTimeMillis())
            
            holder.bind(dayOfMonth, moodForDate, isSelected, isToday) {
                onDateClick(dateInMillis)
            }
        }
    }

    override fun getItemCount(): Int = 42 // 6 weeks * 7 days

    private fun getMoodForDate(dateInMillis: Long): String? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = dateFormat.format(Date(dateInMillis))
        
        val dayEntries = moodEntries.filter { it.date == dateString }
        if (dayEntries.isEmpty()) return null
        
        // Calculate average mood for the day
        val avgValue = dayEntries.map { MoodEntry.moodToValue(it.moodType) }.average()
        
        return when {
            avgValue >= 4.5 -> "😄"
            avgValue >= 3.5 -> "😊"
            avgValue >= 2.5 -> "😐"
            avgValue >= 1.5 -> "😔"
            else -> "😢"
        }
    }

    private fun isSameDay(date1: Long, date2: Long): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.timeInMillis = date1
        cal2.timeInMillis = date2
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        private val moodEmoji: TextView = itemView.findViewById(R.id.moodEmoji)
        private val dateContainer: View = itemView.findViewById(R.id.dateContainer)

        fun bind(day: Int, moodEmoji: String?, isSelected: Boolean, isToday: Boolean, onClick: () -> Unit) {
            dateText.text = day.toString()
            
            if (moodEmoji != null) {
                this.moodEmoji.text = moodEmoji
                this.moodEmoji.visibility = View.VISIBLE
            } else {
                this.moodEmoji.visibility = View.GONE
            }

            // Set background based on state
            when {
                isSelected -> {
                    dateContainer.setBackgroundResource(R.drawable.bg_calendar_selected)
                    dateText.setTextColor(itemView.context.getColor(R.color.white))
                }
                isToday -> {
                    dateContainer.setBackgroundResource(R.drawable.bg_calendar_today)
                    dateText.setTextColor(itemView.context.getColor(R.color.primary))
                }
                else -> {
                    dateContainer.setBackgroundResource(R.drawable.bg_calendar_normal)
                    dateText.setTextColor(itemView.context.getColor(R.color.text_primary))
                }
            }

            itemView.setOnClickListener { onClick() }
        }

        fun bindEmpty() {
            dateText.text = ""
            moodEmoji.visibility = View.GONE
            dateContainer.setBackgroundResource(R.drawable.bg_calendar_empty)
            itemView.setOnClickListener(null)
        }
    }
}
