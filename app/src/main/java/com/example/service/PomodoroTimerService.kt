package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import com.example.model.PomodoroState
import com.example.model.TimerStatus
import com.example.timer.PomodoroTimerManager

class PomodoroTimerService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        PomodoroNotificationHelper.createNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val manager = PomodoroTimerManager.getInstance(this)
        val action = intent?.action

        when (action) {
            PomodoroNotificationHelper.ACTION_START -> manager.start()
            PomodoroNotificationHelper.ACTION_PAUSE -> manager.pause()
            PomodoroNotificationHelper.ACTION_RESUME -> manager.resume()
            PomodoroNotificationHelper.ACTION_STOP -> {
                manager.stop()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            PomodoroNotificationHelper.ACTION_RESET -> manager.reset()
            PomodoroNotificationHelper.ACTION_SKIP -> manager.skip()
        }

        val currentState = manager.state.value
        val notification = PomodoroNotificationHelper.buildNotification(this, currentState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
            startForeground(PomodoroNotificationHelper.NOTIFICATION_ID, notification, type)
        } else {
            startForeground(PomodoroNotificationHelper.NOTIFICATION_ID, notification)
        }

        if (currentState.status == TimerStatus.IDLE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return START_STICKY
    }

    companion object {
        const val ACTION_SYNC = "com.example.pomodoro.ACTION_SYNC"

        fun updateServiceNotification(context: Context, state: PomodoroState) {
            try {
                if (state.status == TimerStatus.RUNNING || state.status == TimerStatus.PAUSED) {
                    val notification = PomodoroNotificationHelper.buildNotification(context, state)
                    val manager = NotificationManagerCompat.from(context)
                    if (manager.areNotificationsEnabled()) {
                        manager.notify(PomodoroNotificationHelper.NOTIFICATION_ID, notification)
                    }
                }
            } catch (_: SecurityException) {
            } catch (_: Exception) {
            }
        }
    }
}
