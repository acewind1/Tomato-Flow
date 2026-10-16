package com.example.timer

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import com.example.data.PomodoroPreferences
import com.example.model.PomodoroSettings
import com.example.model.PomodoroState
import com.example.model.TimerMode
import com.example.model.TimerStatus
import com.example.service.PomodoroTimerService
import com.example.widget.PomodoroWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PomodoroTimerManager private constructor(private val appContext: Context) {

    private val preferences = PomodoroPreferences(appContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var timerJob: Job? = null

    private val _settings = MutableStateFlow(preferences.loadSettings())
    val settings: StateFlow<PomodoroSettings> = _settings.asStateFlow()

    private val _state = MutableStateFlow(
        PomodoroState(
            mode = TimerMode.WORK,
            status = TimerStatus.IDLE,
            remainingSeconds = _settings.value.workMinutes * 60L,
            totalSeconds = _settings.value.workMinutes * 60L,
            currentRound = 1,
            totalRounds = _settings.value.totalRounds,
            todayFocusMinutes = preferences.getTodayFocusMinutes(),
            todayCompletedTomatoes = preferences.getTodayCompletedTomatoes()
        )
    )
    val state: StateFlow<PomodoroState> = _state.asStateFlow()

    fun updateSettings(newSettings: PomodoroSettings) {
        preferences.saveSettings(newSettings)
        _settings.value = newSettings

        val currentState = _state.value
        val newTotalSec = newSettings.durationForMode(currentState.mode) * 60L

        if (currentState.status == TimerStatus.IDLE) {
            _state.value = currentState.copy(
                remainingSeconds = newTotalSec,
                totalSeconds = newTotalSec,
                totalRounds = newSettings.totalRounds
            )
        } else {
            _state.value = currentState.copy(
                totalRounds = newSettings.totalRounds
            )
        }
        notifyExternalComponents()
    }

    fun start() {
        if (_state.value.status == TimerStatus.RUNNING) return

        _state.value = _state.value.copy(status = TimerStatus.RUNNING)
        startServiceIfNeeded()
        startTicking()
        notifyExternalComponents()
    }

    fun pause() {
        if (_state.value.status != TimerStatus.RUNNING) return
        timerJob?.cancel()
        timerJob = null
        _state.value = _state.value.copy(status = TimerStatus.PAUSED)
        notifyExternalComponents()
    }

    fun resume() {
        if (_state.value.status == TimerStatus.PAUSED) {
            start()
        }
    }

    fun togglePlayPause() {
        if (_state.value.status == TimerStatus.RUNNING) {
            pause()
        } else {
            start()
        }
    }

    fun stop() {
        timerJob?.cancel()
        timerJob = null
        val duration = _settings.value.durationForMode(_state.value.mode) * 60L
        _state.value = _state.value.copy(
            status = TimerStatus.IDLE,
            remainingSeconds = duration,
            totalSeconds = duration
        )
        stopService()
        notifyExternalComponents()
    }

    fun reset() {
        val wasRunning = _state.value.status == TimerStatus.RUNNING
        timerJob?.cancel()
        timerJob = null
        val duration = _settings.value.durationForMode(_state.value.mode) * 60L
        _state.value = _state.value.copy(
            status = if (wasRunning) TimerStatus.RUNNING else TimerStatus.IDLE,
            remainingSeconds = duration,
            totalSeconds = duration
        )
        if (wasRunning) {
            startTicking()
        } else {
            stopService()
        }
        notifyExternalComponents()
    }

    fun skip() {
        timerJob?.cancel()
        timerJob = null
        advanceToNextMode(autoStart = false)
        stopService()
        notifyExternalComponents()
    }

    fun selectMode(mode: TimerMode) {
        if (_state.value.mode == mode && _state.value.status == TimerStatus.IDLE) return
        timerJob?.cancel()
        timerJob = null
        val duration = _settings.value.durationForMode(mode) * 60L
        _state.value = _state.value.copy(
            mode = mode,
            status = TimerStatus.IDLE,
            remainingSeconds = duration,
            totalSeconds = duration
        )
        stopService()
        notifyExternalComponents()
    }

    fun setQuickMinutes(minutes: Int) {
        timerJob?.cancel()
        timerJob = null
        val sec = minutes * 60L
        _state.value = _state.value.copy(
            status = TimerStatus.IDLE,
            remainingSeconds = sec,
            totalSeconds = sec
        )
        stopService()
        notifyExternalComponents()
    }

    private fun startTicking() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && _state.value.status == TimerStatus.RUNNING) {
                delay(1000L)
                val currentSec = _state.value.remainingSeconds
                if (currentSec > 1) {
                    _state.value = _state.value.copy(remainingSeconds = currentSec - 1)
                    notifyExternalComponents()
                } else {
                    // Timer Completed!
                    _state.value = _state.value.copy(remainingSeconds = 0)
                    notifyExternalComponents()
                    onTimerFinished()
                    break
                }
            }
        }
    }

    private fun onTimerFinished() {
        timerJob?.cancel()
        timerJob = null

        triggerVibrate()

        val current = _state.value
        val currentSettings = _settings.value

        if (current.mode == TimerMode.WORK) {
            // Record completed work stats
            preferences.recordCompletedWork(currentSettings.workMinutes)
            val updatedMins = preferences.getTodayFocusMinutes()
            val updatedTomatoes = preferences.getTodayCompletedTomatoes()

            if (current.currentRound >= currentSettings.totalRounds) {
                // Time for Long Break!
                val longBreakSec = currentSettings.longBreakMinutes * 60L
                _state.value = current.copy(
                    mode = TimerMode.LONG_BREAK,
                    status = if (currentSettings.autoStartBreak) TimerStatus.RUNNING else TimerStatus.COMPLETED,
                    remainingSeconds = longBreakSec,
                    totalSeconds = longBreakSec,
                    currentRound = 1,
                    todayFocusMinutes = updatedMins,
                    todayCompletedTomatoes = updatedTomatoes
                )
                if (currentSettings.autoStartBreak) {
                    startTicking()
                } else {
                    stopService()
                }
            } else {
                // Short break
                val shortBreakSec = currentSettings.shortBreakMinutes * 60L
                _state.value = current.copy(
                    mode = TimerMode.SHORT_BREAK,
                    status = if (currentSettings.autoStartBreak) TimerStatus.RUNNING else TimerStatus.COMPLETED,
                    remainingSeconds = shortBreakSec,
                    totalSeconds = shortBreakSec,
                    todayFocusMinutes = updatedMins,
                    todayCompletedTomatoes = updatedTomatoes
                )
                if (currentSettings.autoStartBreak) {
                    startTicking()
                } else {
                    stopService()
                }
            }
        } else {
            // Finished Break -> Advance round if coming from short break
            val nextRound = if (current.mode == TimerMode.SHORT_BREAK) {
                (current.currentRound + 1).coerceAtMost(currentSettings.totalRounds)
            } else {
                1
            }
            val workSec = currentSettings.workMinutes * 60L
            _state.value = current.copy(
                mode = TimerMode.WORK,
                status = if (currentSettings.autoStartWork) TimerStatus.RUNNING else TimerStatus.COMPLETED,
                remainingSeconds = workSec,
                totalSeconds = workSec,
                currentRound = nextRound
            )
            if (currentSettings.autoStartWork) {
                startTicking()
            } else {
                stopService()
            }
        }
        notifyExternalComponents()
    }

    private fun advanceToNextMode(autoStart: Boolean) {
        val current = _state.value
        val s = _settings.value
        when (current.mode) {
            TimerMode.WORK -> {
                if (current.currentRound >= s.totalRounds) {
                    val sec = s.longBreakMinutes * 60L
                    _state.value = current.copy(
                        mode = TimerMode.LONG_BREAK,
                        status = if (autoStart) TimerStatus.RUNNING else TimerStatus.IDLE,
                        remainingSeconds = sec,
                        totalSeconds = sec
                    )
                } else {
                    val sec = s.shortBreakMinutes * 60L
                    _state.value = current.copy(
                        mode = TimerMode.SHORT_BREAK,
                        status = if (autoStart) TimerStatus.RUNNING else TimerStatus.IDLE,
                        remainingSeconds = sec,
                        totalSeconds = sec
                    )
                }
            }
            TimerMode.SHORT_BREAK -> {
                val nextRound = (current.currentRound + 1).coerceAtMost(s.totalRounds)
                val sec = s.workMinutes * 60L
                _state.value = current.copy(
                    mode = TimerMode.WORK,
                    status = if (autoStart) TimerStatus.RUNNING else TimerStatus.IDLE,
                    remainingSeconds = sec,
                    totalSeconds = sec,
                    currentRound = nextRound
                )
            }
            TimerMode.LONG_BREAK -> {
                val sec = s.workMinutes * 60L
                _state.value = current.copy(
                    mode = TimerMode.WORK,
                    status = if (autoStart) TimerStatus.RUNNING else TimerStatus.IDLE,
                    remainingSeconds = sec,
                    totalSeconds = sec,
                    currentRound = 1
                )
            }
        }
        if (autoStart) {
            startTicking()
        }
    }

    private fun triggerVibrate() {
        if (!_settings.value.soundVibrate) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400, 200, 600), -1)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 400, 200, 400, 200, 600), -1)
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun startServiceIfNeeded() {
        try {
            val intent = Intent(appContext, PomodoroTimerService::class.java).apply {
                action = PomodoroTimerService.ACTION_SYNC
            }
            ContextCompat.startForegroundService(appContext, intent)
        } catch (_: Exception) {
        }
    }

    private fun stopService() {
        try {
            val intent = Intent(appContext, PomodoroTimerService::class.java)
            appContext.stopService(intent)
        } catch (_: Exception) {
        }
    }

    fun notifyExternalComponents() {
        // Update App Widget
        PomodoroWidgetProvider.updateAllWidgets(appContext, _state.value)
        // If service is running, update notification
        PomodoroTimerService.updateServiceNotification(appContext, _state.value)
    }

    companion object {
        @Volatile
        private var instance: PomodoroTimerManager? = null

        fun getInstance(context: Context): PomodoroTimerManager {
            return instance ?: synchronized(this) {
                instance ?: PomodoroTimerManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
