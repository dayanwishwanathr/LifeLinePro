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
            // Get habit completion percentage
            val prefsManager = SharedPreferencesManager.getInstance(context)
            val percentage = prefsManager.getTodayHabitCompletionPercentage()
            
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
                setTextViewText(R.id.widgetPercentage, "$percentage%")
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

