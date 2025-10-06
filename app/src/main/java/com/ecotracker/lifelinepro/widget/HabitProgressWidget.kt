package com.ecotracker.lifelinepro.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.ecotracker.lifelinepro.MainActivity
import com.ecotracker.lifelinepro.R
import com.ecotracker.lifelinepro.data.repository.SharedPreferencesManager

/**
 * Widget that displays today's habit completion percentage on the home screen
 */
class HabitProgressWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update all widgets
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Called when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Called when the last widget is removed
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefsManager = SharedPreferencesManager.getInstance(context)
            
            // Get today's data
            val habits = prefsManager.getHabits()
            val completedHabits = habits.count { it.isCompleted }
            val totalHabits = habits.size
            val habitsProgress = if (totalHabits > 0) {
                ((completedHabits.toFloat() / totalHabits) * 100).toInt()
            } else 0
            
            // Get hydration data
            val waterIntake = prefsManager.getTodayWaterIntake()
            val settings = prefsManager.getUserSettings()
            val hydrationProgress = if (settings.dailyWaterGoalMl > 0) {
                ((waterIntake.toFloat() / settings.dailyWaterGoalMl) * 100).toInt().coerceAtMost(100)
            } else 0
            
            // Calculate overall progress (average of habits and hydration)
            val overallProgress = if (totalHabits > 0) {
                ((habitsProgress + hydrationProgress) / 2)
            } else hydrationProgress
            
            // Get today's mood
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val today = dateFormat.format(java.util.Date())
            val moodEntries = prefsManager.getMoodEntries().filter { it.date == today }
            
            val averageMoodEmoji = if (moodEntries.isNotEmpty()) {
                val totalMoodValue = moodEntries.sumOf { 
                    com.ecotracker.lifelinepro.data.models.MoodEntry.moodToValue(it.moodType).toDouble()
                }
                val averageValue = (totalMoodValue / moodEntries.size).toFloat()
                
                // Convert average back to emoji
                when {
                    averageValue >= 4.5f -> "😄" // Very Happy
                    averageValue >= 3.5f -> "😊" // Happy
                    averageValue >= 2.5f -> "😐" // Neutral
                    averageValue >= 1.5f -> "😔" // Sad
                    else -> "😢" // Very Sad
                }
            } else {
                "😊" // Default emoji if no mood logged
            }
            
            // Create intent to open app when widget is clicked
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.widget_habit_progress).apply {
                // Set overall progress
                setTextViewText(R.id.widgetOverallProgress, "$overallProgress%")
                
                // Set habits progress
                setTextViewText(R.id.widgetHabitsProgress, "$completedHabits/$totalHabits")
                
                // Set hydration progress
                setTextViewText(R.id.widgetHydrationProgress, "${waterIntake}ml")
                
                // Set mood emoji
                setTextViewText(R.id.widgetMoodEmoji, averageMoodEmoji)
                
                // Set click intent
                setOnClickPendingIntent(R.id.widgetTitle, pendingIntent)
            }
            
            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        
        /**
         * Update all widgets from anywhere in the app
         */
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, HabitProgressWidget::class.java)
            )
            
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}

