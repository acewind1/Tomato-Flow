package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.PomodoroSettings
import com.example.ui.theme.MintGreen
import com.example.ui.theme.PomodoroRed
import com.example.ui.theme.RoyalPurple

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    initialSettings: PomodoroSettings,
    onDismiss: () -> Unit,
    onSave: (PomodoroSettings) -> Unit
) {
    var workMinutes by remember { mutableIntStateOf(initialSettings.workMinutes) }
    var shortBreakMinutes by remember { mutableIntStateOf(initialSettings.shortBreakMinutes) }
    var longBreakMinutes by remember { mutableIntStateOf(initialSettings.longBreakMinutes) }
    var totalRounds by remember { mutableIntStateOf(initialSettings.totalRounds) }
    var autoStartBreak by remember { mutableStateOf(initialSettings.autoStartBreak) }
    var autoStartWork by remember { mutableStateOf(initialSettings.autoStartWork) }
    var soundVibrate by remember { mutableStateOf(initialSettings.soundVibrate) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .testTag("settings_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog Title Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "番茄时钟自定义设置",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("settings_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Work Duration
                DurationSettingItem(
                    title = "工作时长",
                    subtitle = "单次专注时间",
                    value = workMinutes,
                    unit = "分钟",
                    range = 1..120,
                    accentColor = PomodoroRed,
                    presets = listOf(20, 25, 30, 45, 50, 60),
                    onValueChange = { workMinutes = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Short Break Duration
                DurationSettingItem(
                    title = "短休时长",
                    subtitle = "普通轮次休息",
                    value = shortBreakMinutes,
                    unit = "分钟",
                    range = 1..60,
                    accentColor = MintGreen,
                    presets = listOf(3, 5, 8, 10, 15),
                    onValueChange = { shortBreakMinutes = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Long Break Duration
                DurationSettingItem(
                    title = "深度长休",
                    subtitle = "完成大轮次后的深度放松",
                    value = longBreakMinutes,
                    unit = "分钟",
                    range = 1..90,
                    accentColor = RoyalPurple,
                    presets = listOf(15, 20, 25, 30, 45),
                    onValueChange = { longBreakMinutes = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Total Rounds
                DurationSettingItem(
                    title = "总轮数 (循环周期)",
                    subtitle = "达到该轮数后进入深度长休",
                    value = totalRounds,
                    unit = "轮",
                    range = 1..12,
                    accentColor = Color(0xFFFFB300),
                    presets = listOf(2, 3, 4, 6, 8),
                    onValueChange = { totalRounds = it }
                )

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                // Toggles
                Text(
                    text = "自动化与提示",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                ToggleRow(
                    title = "专注结束自动开始休息",
                    checked = autoStartBreak,
                    onCheckedChange = { autoStartBreak = it }
                )

                ToggleRow(
                    title = "休息结束自动开始专注",
                    checked = autoStartWork,
                    onCheckedChange = { autoStartWork = it }
                )

                ToggleRow(
                    title = "完成震动提示",
                    checked = soundVibrate,
                    onCheckedChange = { soundVibrate = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("settings_cancel_btn")
                    ) {
                        Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            val newSettings = PomodoroSettings(
                                workMinutes = workMinutes,
                                shortBreakMinutes = shortBreakMinutes,
                                longBreakMinutes = longBreakMinutes,
                                totalRounds = totalRounds,
                                autoStartBreak = autoStartBreak,
                                autoStartWork = autoStartWork,
                                soundVibrate = soundVibrate
                            )
                            onSave(newSettings)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("settings_save_btn")
                    ) {
                        Text("保存并应用", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DurationSettingItem(
    title: String,
    subtitle: String,
    value: Int,
    unit: String,
    range: IntRange,
    accentColor: Color,
    presets: List<Int>,
    onValueChange: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Stepper + Current Value
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(
                        onClick = { if (value > range.first) onValueChange(value - 1) },
                        modifier = Modifier.size(36.dp),
                        enabled = value > range.first
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "减少", modifier = Modifier.size(18.dp))
                    }

                    Text(
                        text = "$value $unit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    FilledTonalIconButton(
                        onClick = { if (value < range.last) onValueChange(value + 1) },
                        modifier = Modifier.size(36.dp),
                        enabled = value < range.last
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "增加", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Slider
            var sliderPos by remember(value) { mutableFloatStateOf(value.toFloat()) }
            Slider(
                value = sliderPos,
                onValueChange = {
                    sliderPos = it
                    onValueChange(it.toInt())
                },
                valueRange = range.first.toFloat()..range.last.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = accentColor.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Preset Quick-Pick Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                presets.forEach { preset ->
                    val isSelected = preset == value
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) accentColor else MaterialTheme.colorScheme.surface
                            )
                            .clickable { onValueChange(preset) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$preset$unit",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
