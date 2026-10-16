package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.model.PomodoroState
import com.example.model.TimerMode
import com.example.model.TimerStatus

object PomodoroNotificationHelper {
    const val CHANNEL_ID = "pomodoro_timer_channel"
    const val NOTIFICATION_ID = 1001

    const val ACTION_START = "com.example.pomodoro.ACTION_START"
    const val ACTION_PAUSE = "com.example.pomodoro.ACTION_PAUSE"
    const val ACTION_RESUME = "com.example.pomodoro.ACTION_RESUME"
    const val ACTION_STOP = "com.example.pomodoro.ACTION_STOP"
    const val ACTION_RESET = "com.example.pomodoro.ACTION_RESET"
    const val ACTION_SKIP = "com.example.pomodoro.ACTION_SKIP"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val desc = context.getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = desc
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(context: Context, state: PomodoroState): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val modeLabel = when (state.mode) {
            TimerMode.WORK -> "🍅 专注工作"
            TimerMode.SHORT_BREAK -> "☕ 短暂休息"
            TimerMode.LONG_BREAK -> "🌴 深度休息"
        }

        val statusLabel = when (state.status) {
            TimerStatus.RUNNING -> "倒计时中"
            TimerStatus.PAUSED -> "已暂停"
            TimerStatus.COMPLETED -> "本轮已完成"
            TimerStatus.IDLE -> "准备就绪"
        }

        val title = "$modeLabel (${state.currentRound}/${state.totalRounds}轮) - ${state.formattedTime}"
        val contentText = "$statusLabel · 进度 ${state.progressPercent}%"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notif_play)
            .setContentTitle(title)
            .setContentText(contentText)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(state.status == TimerStatus.RUNNING || state.status == TimerStatus.PAUSED)
            .setOnlyAlertOnce(true)
            .setProgress(100, state.progressPercent, false)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Actions depending on current status
        when (state.status) {
            TimerStatus.RUNNING -> {
                builder.addAction(
                    R.drawable.ic_notif_pause,
                    context.getString(R.string.action_pause),
                    getServicePendingIntent(context, ACTION_PAUSE, 1)
                )
                builder.addAction(
                    R.drawable.ic_notif_skip,
                    context.getString(R.string.action_skip),
                    getServicePendingIntent(context, ACTION_SKIP, 2)
                )
                builder.addAction(
                    R.drawable.ic_notif_stop,
                    context.getString(R.string.action_stop),
                    getServicePendingIntent(context, ACTION_STOP, 3)
                )
            }
            TimerStatus.PAUSED -> {
                builder.addAction(
                    R.drawable.ic_notif_play,
                    context.getString(R.string.action_resume),
                    getServicePendingIntent(context, ACTION_RESUME, 4)
                )
                builder.addAction(
                    R.drawable.ic_notif_reset,
                    context.getString(R.string.action_reset),
                    getServicePendingIntent(context, ACTION_RESET, 5)
                )
                builder.addAction(
                    R.drawable.ic_notif_skip,
                    context.getString(R.string.action_skip),
                    getServicePendingIntent(context, ACTION_SKIP, 6)
                )
            }
            TimerStatus.IDLE, TimerStatus.COMPLETED -> {
                builder.addAction(
                    R.drawable.ic_notif_play,
                    context.getString(R.string.action_start),
                    getServicePendingIntent(context, ACTION_START, 7)
                )
            }
        }

        return builder.build()
    }

    private fun getServicePendingIntent(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, PomodoroTimerService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
