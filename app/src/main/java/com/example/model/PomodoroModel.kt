package com.example.model

enum class TimerMode(val title: String) {
    WORK("专注工作"),
    SHORT_BREAK("短暂休息"),
    LONG_BREAK("深度休息")
}

enum class TimerStatus {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED
}

data class PomodoroSettings(
    val workMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val totalRounds: Int = 4,
    val autoStartBreak: Boolean = true,
    val autoStartWork: Boolean = false,
    val soundVibrate: Boolean = true
) {
    fun durationForMode(mode: TimerMode): Int {
        return when (mode) {
            TimerMode.WORK -> workMinutes
            TimerMode.SHORT_BREAK -> shortBreakMinutes
            TimerMode.LONG_BREAK -> longBreakMinutes
        }
    }
}

data class PomodoroState(
    val mode: TimerMode = TimerMode.WORK,
    val status: TimerStatus = TimerStatus.IDLE,
    val remainingSeconds: Long = 25 * 60L,
    val totalSeconds: Long = 25 * 60L,
    val currentRound: Int = 1,
    val totalRounds: Int = 4,
    val todayFocusMinutes: Int = 0,
    val todayCompletedTomatoes: Int = 0
) {
    val progress: Float
        get() = if (totalSeconds > 0) {
            ((totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val formattedTime: String
        get() {
            val minutes = (remainingSeconds / 60).coerceAtLeast(0)
            val seconds = (remainingSeconds % 60).coerceAtLeast(0)
            return String.format("%02d:%02d", minutes, seconds)
        }

    val progressPercent: Int
        get() = (progress * 100).toInt().coerceIn(0, 100)
}
