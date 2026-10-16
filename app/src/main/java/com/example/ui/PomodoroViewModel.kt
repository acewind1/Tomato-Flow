package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.model.PomodoroSettings
import com.example.model.PomodoroState
import com.example.model.TimerMode
import com.example.timer.PomodoroTimerManager
import kotlinx.coroutines.flow.StateFlow

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {
    private val manager = PomodoroTimerManager.getInstance(application)

    val state: StateFlow<PomodoroState> = manager.state
    val settings: StateFlow<PomodoroSettings> = manager.settings

    fun start() = manager.start()
    fun pause() = manager.pause()
    fun resume() = manager.resume()
    fun togglePlayPause() = manager.togglePlayPause()
    fun stop() = manager.stop()
    fun reset() = manager.reset()
    fun skip() = manager.skip()
    fun selectMode(mode: TimerMode) = manager.selectMode(mode)
    fun setQuickMinutes(minutes: Int) = manager.setQuickMinutes(minutes)
    fun updateSettings(newSettings: PomodoroSettings) = manager.updateSettings(newSettings)
}
