package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.PomodoroSettings
import com.example.model.PomodoroState
import com.example.model.TimerMode
import com.example.model.TimerStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("番茄时钟", appName)
    }

    @Test
    fun `test pomodoro settings duration and state formatting`() {
        val settings = PomodoroSettings(
            workMinutes = 30,
            shortBreakMinutes = 7,
            longBreakMinutes = 20,
            totalRounds = 4
        )

        assertEquals(30, settings.durationForMode(TimerMode.WORK))
        assertEquals(7, settings.durationForMode(TimerMode.SHORT_BREAK))
        assertEquals(20, settings.durationForMode(TimerMode.LONG_BREAK))

        val state = PomodoroState(
            mode = TimerMode.WORK,
            status = TimerStatus.RUNNING,
            remainingSeconds = 1500L,
            totalSeconds = 1800L
        )

        assertEquals("25:00", state.formattedTime)
        // (1800 - 1500) / 1800 = 300 / 1800 = 16.66% -> 16%
        assertEquals(16, state.progressPercent)
    }
}
