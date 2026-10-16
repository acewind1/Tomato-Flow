package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PomodoroState
import com.example.model.TimerMode
import com.example.model.TimerStatus
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PomodoroRing(
    state: PomodoroState,
    size: Dp = 280.dp,
    strokeWidth: Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = state.progress,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "progress"
    )

    // Vibrant gradients tailored to each mode
    val gradientColors = when (state.mode) {
        TimerMode.WORK -> listOf(
            Color(0xFFFF3366),
            Color(0xFFFF5E3A),
            Color(0xFFFF9500),
            Color(0xFFFF2A6D),
            Color(0xFFFF3366)
        )
        TimerMode.SHORT_BREAK -> listOf(
            Color(0xFF00C9FF),
            Color(0xFF00E676),
            Color(0xFF10B981),
            Color(0xFF00F2FE),
            Color(0xFF00C9FF)
        )
        TimerMode.LONG_BREAK -> listOf(
            Color(0xFF9D50BB),
            Color(0xFF6E48AA),
            Color(0xFF8B5CF6),
            Color(0xFFD946EF),
            Color(0xFF9D50BB)
        )
    }

    val trackColor = when (state.mode) {
        TimerMode.WORK -> Color(0x22FF5722)
        TimerMode.SHORT_BREAK -> Color(0x2210B981)
        TimerMode.LONG_BREAK -> Color(0x228B5CF6)
    }

    val accentHeadColor = when (state.mode) {
        TimerMode.WORK -> Color(0xFFFFD54F)
        TimerMode.SHORT_BREAK -> Color(0xFF69F0AE)
        TimerMode.LONG_BREAK -> Color(0xFFE040FB)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .testTag("pomodoro_ring_container")
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(strokeWidth / 2)) {
            val strokeWidthPx = strokeWidth.toPx()
            val arcSize = Size(this.size.width, this.size.height)
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.width / 2f

            // 1. Draw subtle background track ticks (60 minute marks)
            val totalTicks = 60
            for (i in 0 until totalTicks) {
                val tickAngleDeg = i * (360f / totalTicks) - 90f
                val tickAngleRad = Math.toRadians(tickAngleDeg.toDouble())
                val isMajor = i % 5 == 0
                val tickLen = if (isMajor) strokeWidthPx * 0.45f else strokeWidthPx * 0.25f
                val innerR = radius - strokeWidthPx / 2f - (if (isMajor) 8f else 5f)
                val outerR = innerR - tickLen

                val startX = (center.x + innerR * cos(tickAngleRad)).toFloat()
                val startY = (center.y + innerR * sin(tickAngleRad)).toFloat()
                val endX = (center.x + outerR * cos(tickAngleRad)).toFloat()
                val endY = (center.y + outerR * sin(tickAngleRad)).toFloat()

                drawLine(
                    color = if (isMajor) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.15f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isMajor) 2.5f else 1.2f,
                    cap = StrokeCap.Round
                )
            }

            // 2. Draw background track circle
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // 3. Draw active vibrant sweep gradient arc
            if (animatedProgress > 0.002f) {
                val sweepAngle = 360f * animatedProgress
                val brush = Brush.sweepGradient(
                    colors = gradientColors,
                    center = center
                )

                drawArc(
                    brush = brush,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )

                // 4. Glowing head dot at the tip of the arc
                val headAngleRad = Math.toRadians((-90f + sweepAngle).toDouble())
                val headX = (center.x + radius * cos(headAngleRad)).toFloat()
                val headY = (center.y + radius * sin(headAngleRad)).toFloat()

                // Halo glow
                drawCircle(
                    color = accentHeadColor.copy(alpha = 0.4f),
                    radius = strokeWidthPx * 0.8f,
                    center = Offset(headX, headY)
                )
                // Solid center pin
                drawCircle(
                    color = Color.White,
                    radius = strokeWidthPx * 0.35f,
                    center = Offset(headX, headY)
                )
            }
        }

        // Inner Digital Display & Round Status
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Mode & Status Subtitle
            val statusText = when (state.status) {
                TimerStatus.RUNNING -> if (state.mode == TimerMode.WORK) "专注中" else "休息中"
                TimerStatus.PAUSED -> "已暂停"
                TimerStatus.COMPLETED -> "本轮完成"
                TimerStatus.IDLE -> "准备开始"
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.labelLarge,
                color = when (state.mode) {
                    TimerMode.WORK -> Color(0xFFFF8A65)
                    TimerMode.SHORT_BREAK -> Color(0xFF34D399)
                    TimerMode.LONG_BREAK -> Color(0xFFA78BFA)
                },
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Large Digital Time
            Text(
                text = state.formattedTime,
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("timer_digital_display")
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Round & Progress Details
            Text(
                text = "第 ${state.currentRound} / ${state.totalRounds} 轮 · ${state.progressPercent}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
