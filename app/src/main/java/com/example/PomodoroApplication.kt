package com.example

import android.app.Application
import com.example.service.PomodoroNotificationHelper
import com.example.timer.PomodoroTimerManager

class PomodoroApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PomodoroNotificationHelper.createNotificationChannel(this)
        PomodoroTimerManager.getInstance(this)
    }
}
