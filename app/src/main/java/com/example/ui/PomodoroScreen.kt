package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.PomodoroState
import com.example.model.TimerMode
import com.example.model.TimerStatus
import com.example.ui.components.PomodoroRing
import com.example.ui.components.SettingsDialog
import com.example.ui.theme.MintGreen
import com.example.ui.theme.PomodoroRed
import com.example.ui.theme.RoyalPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    var showSettingsDialog by remember { mutableStateOf(false) }

    // Check notification permission for Android 13+
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🍅 番茄时钟",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("action_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "自定义设置",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Notification permission banner (Android 13+)
                    if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "允许通知可在后台、锁屏常驻查看进度与快捷控制",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.testTag("grant_notification_permission_btn")
                                ) {
                                    Text("开启", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }

                    // Mode Selection Tabs (工作, 短休, 长休)
                    ModeTabsRow(
                        currentMode = state.mode,
                        onModeSelected = { viewModel.selectMode(it) },
                        workMinutes = settings.workMinutes,
                        shortBreakMinutes = settings.shortBreakMinutes,
                        longBreakMinutes = settings.longBreakMinutes
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Vibrant Circular Ring Display with Digital Time
                    PomodoroRing(
                        state = state,
                        size = 280.dp,
                        strokeWidth = 20.dp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Round indicator dots
                    RoundsIndicator(
                        currentRound = state.currentRound,
                        totalRounds = state.totalRounds,
                        mode = state.mode
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Main Controls (Play/Pause, Skip, Reset, Stop)
                    MainControlsRow(
                        status = state.status,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onReset = { viewModel.reset() },
                        onSkip = { viewModel.skip() },
                        onStop = { viewModel.stop() }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Fast Duration Quick-Picks when Idle
                    if (state.status == TimerStatus.IDLE) {
                        QuickDurationSelector(
                            currentMinutes = (state.remainingSeconds / 60).toInt(),
                            onSelectMinutes = { viewModel.setQuickMinutes(it) }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Today's Stats & Feature Showcase Card
                    TodayStatsCard(
                        todayMinutes = state.todayFocusMinutes,
                        todayTomatoes = state.todayCompletedTomatoes,
                        totalRounds = settings.totalRounds
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Home Screen Widget & Notification Info Card
                    WidgetAndNotificationHintCard()

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            initialSettings = settings,
            onDismiss = { showSettingsDialog = false },
            onSave = { newSettings ->
                viewModel.updateSettings(newSettings)
            }
        )
    }
}

@Composable
private fun ModeTabsRow(
    currentMode: TimerMode,
    onModeSelected: (TimerMode) -> Unit,
    workMinutes: Int,
    shortBreakMinutes: Int,
    longBreakMinutes: Int
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mode_tabs_row")
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ModeTabItem(
                title = "专注工作",
                durationText = "${workMinutes}m",
                isSelected = currentMode == TimerMode.WORK,
                accentColor = PomodoroRed,
                modifier = Modifier.weight(1f),
                onClick = { onModeSelected(TimerMode.WORK) }
            )
            ModeTabItem(
                title = "短暂休息",
                durationText = "${shortBreakMinutes}m",
                isSelected = currentMode == TimerMode.SHORT_BREAK,
                accentColor = MintGreen,
                modifier = Modifier.weight(1f),
                onClick = { onModeSelected(TimerMode.SHORT_BREAK) }
            )
            ModeTabItem(
                title = "深度长休",
                durationText = "${longBreakMinutes}m",
                isSelected = currentMode == TimerMode.LONG_BREAK,
                accentColor = RoyalPurple,
                modifier = Modifier.weight(1f),
                onClick = { onModeSelected(TimerMode.LONG_BREAK) }
            )
        }
    }
}

@Composable
private fun ModeTabItem(
    title: String,
    durationText: String,
    isSelected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tab_bg"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = durationText,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun RoundsIndicator(
    currentRound: Int,
    totalRounds: Int,
    mode: TimerMode
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rounds_indicator")
    ) {
        for (i in 1..totalRounds) {
            val isCompleted = i < currentRound
            val isCurrent = i == currentRound

            val dotColor = when {
                isCompleted -> PomodoroRed
                isCurrent -> if (mode == TimerMode.WORK) PomodoroRed else MintGreen
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (isCurrent) 12.dp else 9.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}

@Composable
private fun MainControlsRow(
    status: TimerStatus,
    onTogglePlayPause: () -> Unit,
    onReset: () -> Unit,
    onSkip: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .testTag("main_controls_row"),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Reset Button
        FilledTonalIconButton(
            onClick = onReset,
            modifier = Modifier
                .size(54.dp)
                .testTag("action_reset_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "重置",
                modifier = Modifier.size(24.dp)
            )
        }

        // Primary Play / Pause FAB
        Button(
            onClick = onTogglePlayPause,
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .height(64.dp)
                .widthIn(min = 140.dp)
                .shadow(8.dp, RoundedCornerShape(26.dp))
                .testTag("action_toggle_btn")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (status == TimerStatus.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (status == TimerStatus.RUNNING) "暂停" else "开始",
                    modifier = Modifier.size(30.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (status) {
                        TimerStatus.RUNNING -> "暂停"
                        TimerStatus.PAUSED -> "继续"
                        TimerStatus.IDLE -> "开始专注"
                        TimerStatus.COMPLETED -> "开启新一轮"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Skip Button
        FilledTonalIconButton(
            onClick = onSkip,
            modifier = Modifier
                .size(54.dp)
                .testTag("action_skip_btn")
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "跳过",
                modifier = Modifier.size(24.dp)
            )
        }

        // Stop Button (visible when active)
        if (status == TimerStatus.RUNNING || status == TimerStatus.PAUSED) {
            FilledTonalIconButton(
                onClick = onStop,
                modifier = Modifier
                    .size(54.dp)
                    .testTag("action_stop_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "停止",
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickDurationSelector(
    currentMinutes: Int,
    onSelectMinutes: (Int) -> Unit
) {
    val quickOptions = listOf(15, 20, 25, 30, 45, 60)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "快捷调节本次时长",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            quickOptions.forEach { mins ->
                val isSelected = currentMinutes == mins
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .clickable { onSelectMinutes(mins) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${mins}分",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayStatsCard(
    todayMinutes: Int,
    todayTomatoes: Int,
    totalRounds: Int
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "今日专注统计",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "目标周期: $totalRounds 轮",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(
                    emoji = "🍅",
                    value = "$todayTomatoes",
                    label = "收获番茄"
                )
                StatItem(
                    emoji = "⏱️",
                    value = "$todayMinutes",
                    label = "专注分钟"
                )
                StatItem(
                    emoji = "🔥",
                    value = "${todayMinutes * 60 / 3600}h ${todayMinutes % 60}m",
                    label = "累计耗时"
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    emoji: String,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WidgetAndNotificationHintCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "桌面小部件与通知栏控制",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• 桌面微件：返回手机桌面长按空白处，添加「番茄时钟」微件，直接查看时间并快捷操作。\n• 常驻通知栏：计时开始后，可在通知中心随时查看剩余时间、暂停、继续、跳过或停止。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
