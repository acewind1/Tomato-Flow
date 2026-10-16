package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.PomodoroSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PomodoroPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("pomodoro_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_WORK_MINUTES = "work_minutes"
        private const val KEY_SHORT_BREAK_MINUTES = "short_break_minutes"
        private const val KEY_LONG_BREAK_MINUTES = "long_break_minutes"
        private const val KEY_TOTAL_ROUNDS = "total_rounds"
        private const val KEY_AUTO_START_BREAK = "auto_start_break"
        private const val KEY_AUTO_START_WORK = "auto_start_work"
        private const val KEY_SOUND_VIBRATE = "sound_vibrate"

        private const val KEY_STATS_DATE = "stats_date"
        private const val KEY_TODAY_FOCUS_MINUTES = "today_focus_minutes"
        private const val KEY_TODAY_COMPLETED_TOMATOES = "today_completed_tomatoes"
    }

    fun loadSettings(): PomodoroSettings {
        return PomodoroSettings(
            workMinutes = prefs.getInt(KEY_WORK_MINUTES, 25),
            shortBreakMinutes = prefs.getInt(KEY_SHORT_BREAK_MINUTES, 5),
            longBreakMinutes = prefs.getInt(KEY_LONG_BREAK_MINUTES, 15),
            totalRounds = prefs.getInt(KEY_TOTAL_ROUNDS, 4),
            autoStartBreak = prefs.getBoolean(KEY_AUTO_START_BREAK, true),
            autoStartWork = prefs.getBoolean(KEY_AUTO_START_WORK, false),
            soundVibrate = prefs.getBoolean(KEY_SOUND_VIBRATE, true)
        )
    }

    fun saveSettings(settings: PomodoroSettings) {
        prefs.edit()
            .putInt(KEY_WORK_MINUTES, settings.workMinutes)
            .putInt(KEY_SHORT_BREAK_MINUTES, settings.shortBreakMinutes)
            .putInt(KEY_LONG_BREAK_MINUTES, settings.longBreakMinutes)
            .putInt(KEY_TOTAL_ROUNDS, settings.totalRounds)
            .putBoolean(KEY_AUTO_START_BREAK, settings.autoStartBreak)
            .putBoolean(KEY_AUTO_START_WORK, settings.autoStartWork)
            .putBoolean(KEY_SOUND_VIBRATE, settings.soundVibrate)
            .apply()
    }

    private fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    }

    private fun checkDateRollover() {
        val savedDate = prefs.getString(KEY_STATS_DATE, "")
        val today = getCurrentDateString()
        if (savedDate != today) {
            prefs.edit()
                .putString(KEY_STATS_DATE, today)
                .putInt(KEY_TODAY_FOCUS_MINUTES, 0)
                .putInt(KEY_TODAY_COMPLETED_TOMATOES, 0)
                .apply()
        }
    }

    fun getTodayFocusMinutes(): Int {
        checkDateRollover()
        return prefs.getInt(KEY_TODAY_FOCUS_MINUTES, 0)
    }

    fun getTodayCompletedTomatoes(): Int {
        checkDateRollover()
        return prefs.getInt(KEY_TODAY_COMPLETED_TOMATOES, 0)
    }

    fun recordCompletedWork(minutes: Int) {
        checkDateRollover()
        val currentMins = prefs.getInt(KEY_TODAY_FOCUS_MINUTES, 0)
        val currentTomatoes = prefs.getInt(KEY_TODAY_COMPLETED_TOMATOES, 0)
        prefs.edit()
            .putInt(KEY_TODAY_FOCUS_MINUTES, currentMins + minutes)
            .putInt(KEY_TODAY_COMPLETED_TOMATOES, currentTomatoes + 1)
            .apply()
    }
}
