package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.model.PomodoroState
import com.example.model.TimerMode
import com.example.model.TimerStatus
import com.example.timer.PomodoroTimerManager

class PomodoroWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val state = PomodoroTimerManager.getInstance(context).state.value
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId, state)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val manager = PomodoroTimerManager.getInstance(context)
        when (intent.action) {
            ACTION_TOGGLE -> manager.togglePlayPause()
            ACTION_RESET -> manager.reset()
            ACTION_SKIP -> manager.skip()
        }
    }

    companion object {
        const val ACTION_TOGGLE = "com.example.pomodoro.ACTION_WIDGET_TOGGLE"
        const val ACTION_RESET = "com.example.pomodoro.ACTION_WIDGET_RESET"
        const val ACTION_SKIP = "com.example.pomodoro.ACTION_WIDGET_SKIP"

        fun updateAllWidgets(context: Context, state: PomodoroState) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val componentName = ComponentName(context, PomodoroWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                for (widgetId in appWidgetIds) {
                    updateWidget(context, appWidgetManager, widgetId, state)
                }
            }
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int,
            state: PomodoroState
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_pomodoro)

            val modeTitle = when (state.mode) {
                TimerMode.WORK -> "🍅 专注工作"
                TimerMode.SHORT_BREAK -> "☕ 短暂休息"
                TimerMode.LONG_BREAK -> "🌴 深度休息"
            }
            views.setTextViewText(R.id.widget_mode_title, modeTitle)
            views.setTextViewText(R.id.widget_round_text, "第 ${state.currentRound} / ${state.totalRounds} 轮")
            views.setTextViewText(R.id.widget_timer_text, state.formattedTime)
            views.setTextViewText(R.id.widget_percent_text, "${state.progressPercent}%")
            views.setProgressBar(R.id.widget_progress_bar, 100, state.progressPercent, false)

            val toggleIcon = if (state.status == TimerStatus.RUNNING) {
                R.drawable.ic_notif_pause
            } else {
                R.drawable.ic_notif_play
            }
            views.setImageViewResource(R.id.widget_btn_toggle, toggleIcon)

            // Click root -> Open App
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

            // Controls
            views.setOnClickPendingIntent(
                R.id.widget_btn_toggle,
                getBroadcastPendingIntent(context, ACTION_TOGGLE, 101)
            )
            views.setOnClickPendingIntent(
                R.id.widget_btn_reset,
                getBroadcastPendingIntent(context, ACTION_RESET, 102)
            )
            views.setOnClickPendingIntent(
                R.id.widget_btn_skip,
                getBroadcastPendingIntent(context, ACTION_SKIP, 103)
            )

            appWidgetManager.updateAppWidget(widgetId, views)
        }

        private fun getBroadcastPendingIntent(context: Context, action: String, requestCode: Int): PendingIntent {
            val intent = Intent(context, PomodoroWidgetProvider::class.java).apply {
                this.action = action
            }
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
