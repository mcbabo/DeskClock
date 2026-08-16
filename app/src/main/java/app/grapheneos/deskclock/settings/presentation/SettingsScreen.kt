package app.grapheneos.deskclock.settings.presentation

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.exitUntilCollapsedScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.alarm.presentation.components.RingtonePickerDialog
import app.grapheneos.deskclock.core.presentation.components.groupitems.ListGroup
import app.grapheneos.deskclock.core.presentation.components.groupitems.SwitchGroupRow
import app.grapheneos.deskclock.core.presentation.components.groupitems.ValueGroupRow
import app.grapheneos.deskclock.core.presentation.screenPadding
import app.grapheneos.deskclock.core.ringtone.RingtoneItem
import app.grapheneos.deskclock.core.theme.DeskClockTheme
import app.grapheneos.deskclock.settings.data.PopUpStyle
import app.grapheneos.deskclock.settings.data.ThemeMode
import app.grapheneos.deskclock.settings.presentation.components.GraduallyIncreaseVolumeDrawer
import app.grapheneos.deskclock.settings.presentation.components.RingtoneVolumeDrawer
import app.grapheneos.deskclock.settings.presentation.components.SnoozeDrawer
import app.grapheneos.deskclock.settings.presentation.components.ThemeDrawer
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
    onNavigateToAlarmStylePicker: () -> Unit,
    onNavigateToTimerStylePicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val scrollBehavior = exitUntilCollapsedScrollBehavior()

    var showSnoozeDialog by remember { mutableStateOf(false) }
    var showRingtoneDialog by remember { mutableStateOf(false) }
    var showDirectBootRingtoneDialog by remember { mutableStateOf(false) }
    var showRingtoneVolumeDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showGraduallyIncreaseVolumeDialog by remember { mutableStateOf(false) }

    val settings = state.settings ?: return

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .screenPadding()
        ) {
            GeneralSettingsSection(
                dynamicColors = settings.dynamicColors,
                themeMode = settings.themeMode,
                alarmPopUpStyle = settings.alarmPopUpStyle,
                timerPopUpStyle = settings.timerPopUpStyle,
                onDynamicColorsChange = { onIntent(SettingsIntent.SetDynamicColors(it)) },
                onThemeClick = { showThemeDialog = true },
                onAlarmPopUpStyleClick = onNavigateToAlarmStylePicker,
                onTimerPopUpStyleClick = onNavigateToTimerStylePicker
            )

            AlarmTimerSettingsSection(
                settings = settings,
                onSnoozeClick = { showSnoozeDialog = true },
                onRingtoneClick = { showRingtoneDialog = true },
                onDirectBootRingtoneClick = { showDirectBootRingtoneDialog = true },
                onVolumeClick = { showRingtoneVolumeDialog = true },
                onVibrationChange = { onIntent(SettingsIntent.SetDefaultVibration(it)) },
                onGraduallyIncreaseVolumeClick = {
                    showGraduallyIncreaseVolumeDialog = true
                }
            )
        }
    }

    if (showSnoozeDialog) {
        SnoozeDrawer(
            settings.snoozeDurationMinutes,
            onConfirm = {
                onIntent(SettingsIntent.SetSnoozeTime(it))
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                showSnoozeDialog = false
            }
        ) {
            showSnoozeDialog = false
        }
    }

    if (showRingtoneDialog) {
        RingtonePickerDialog(
            ringtones = state.ringtones,
            initialUri = settings.defaultRingtone.uri,
            onPlayPreview = { onIntent(SettingsIntent.PlayPreview(it)) },
            onStopPreview = { onIntent(SettingsIntent.StopPreview) },
            onDismiss = {
                onIntent(SettingsIntent.StopPreview)
                showRingtoneDialog = false
            },
            onConfirm = {
                onIntent(SettingsIntent.SetDefaultRingtone(it))
                showRingtoneDialog = false
            }
        )
    }

    if (showDirectBootRingtoneDialog) {
        RingtonePickerDialog(
            ringtones = state.rawRingtones,
            initialUri = settings.directBootRingtone.uri,
            onPlayPreview = { onIntent(SettingsIntent.PlayPreview(it)) },
            onStopPreview = { onIntent(SettingsIntent.StopPreview) },
            onDismiss = {
                onIntent(SettingsIntent.StopPreview)
                showDirectBootRingtoneDialog = false
            },
            onConfirm = {
                onIntent(SettingsIntent.SetDirectBootRingtone(it))
                showDirectBootRingtoneDialog = false
            }
        )
    }

    if (showRingtoneVolumeDialog) {
        RingtoneVolumeDrawer(
            customRingtoneVolumeEnabled = settings.useCustomRingtoneVolume,
            currentVolume = settings.ringtoneVolume,
            onChange = { enabled, volume ->
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                onIntent(SettingsIntent.SetCustomRingtoneVolumeEnabled(enabled))
                onIntent(SettingsIntent.SetCustomRingtoneVolume(volume))
                showRingtoneVolumeDialog = false
            }
        ) {
            showRingtoneVolumeDialog = false
        }
    }

    if (showThemeDialog) {
        ThemeDrawer(
            settings.themeMode,
            onThemeChange = {
                onIntent(SettingsIntent.UpdateTheme(it))
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                showThemeDialog = false
            },
            onDismissRequest = { showThemeDialog = false }
        )
    }

    if (showGraduallyIncreaseVolumeDialog) {
        GraduallyIncreaseVolumeDrawer(
            initiallyEnabled = settings.graduallyIncreaseVolume,
            initialDuration = settings.graduallyIncreaseVolumeDuration,
            onConfirm = { enabled, duration ->
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                onIntent(SettingsIntent.SetGraduallyIncreaseVolume(enabled))
                onIntent(SettingsIntent.SetGraduallyIncreaseVolumeDuration(duration))
                showGraduallyIncreaseVolumeDialog = false
            },
            onDismiss = { showGraduallyIncreaseVolumeDialog = false }
        )
    }
}

@Composable
private fun GeneralSettingsSection(
    dynamicColors: Boolean,
    themeMode: ThemeMode,
    alarmPopUpStyle: PopUpStyle,
    timerPopUpStyle: PopUpStyle,
    onDynamicColorsChange: (Boolean) -> Unit,
    onThemeClick: () -> Unit,
    onAlarmPopUpStyleClick: () -> Unit,
    onTimerPopUpStyleClick: () -> Unit
) {
    ListGroup(title = stringResource(R.string.general)) {
        item {
            SwitchGroupRow(
                label = stringResource(R.string.settings_dynamic_colors),
                supportingContent = {
                    Text(text = stringResource(R.string.settings_dynamic_colors_desc))
                },
                checked = dynamicColors,
                onCheckedChange = onDynamicColorsChange,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.ColorLens,
                        contentDescription = stringResource(R.string.settings_dynamic_colors)
                    )
                }
            )
        }

        item {
            ValueGroupRow(
                label = stringResource(R.string.setting_theme),
                value = stringResource(themeMode.displayNameRes),
                supportingContent = {
                    Text(text = stringResource(R.string.setting_theme_desc))
                },
                onClick = onThemeClick,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Colorize,
                        contentDescription = stringResource(R.string.setting_theme)
                    )
                }
            )
        }

        item {
            ValueGroupRow(
                label = stringResource(R.string.settings_alarm_popup_style),
                value = stringResource(alarmPopUpStyle.titleRes),
                supportingContent = {
                    Text(text = stringResource(R.string.settings_alarm_popup_style_desc))
                },
                onClick = onAlarmPopUpStyleClick,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = null
                    )
                }
            )
        }

        item {
            ValueGroupRow(
                label = stringResource(R.string.settings_timer_popup_style),
                value = stringResource(timerPopUpStyle.titleRes),
                supportingContent = {
                    Text(text = stringResource(R.string.settings_timer_popup_style_desc))
                },
                onClick = onTimerPopUpStyleClick,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = null
                    )
                }
            )
        }
    }
}

@Composable
private fun AlarmTimerSettingsSection(
    settings: AppSettingsUiModel,
    onSnoozeClick: () -> Unit,
    onRingtoneClick: () -> Unit,
    onDirectBootRingtoneClick: () -> Unit,
    onVolumeClick: () -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    onGraduallyIncreaseVolumeClick: () -> Unit
) {
    ListGroup(title = stringResource(R.string.settings_alarm_and_timer)) {
        item {
            ValueGroupRow(
                label = stringResource(R.string.settings_snooze_duration),
                value = stringResource(
                    R.string.n_minutes,
                    settings.snoozeDurationMinutes
                ),
                supportingContent = {
                    Text(text = stringResource(R.string.settings_snooze_duration_desc))
                },
                onClick = onSnoozeClick,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Snooze,
                        contentDescription = stringResource(R.string.settings_snooze_duration)
                    )
                }
            )
        }

        item {
            ValueGroupRow(
                label = stringResource(R.string.settings_alarm_sound),
                value = settings.defaultRingtone.name,
                supportingContent = {
                    Text(text = stringResource(R.string.settings_alarm_sound_desc))
                },
                onClick = onRingtoneClick,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.MusicNote,
                        contentDescription = stringResource(R.string.settings_alarm_sound)
                    )
                }
            )
        }

        item {
            ValueGroupRow(
                label = stringResource(R.string.settings_direct_boot_sound),
                value = settings.directBootRingtone.name,
                supportingContent = {
                    Text(text = stringResource(R.string.settings_direct_boot_sound_desc))
                },
                onClick = onDirectBootRingtoneClick,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.SystemUpdate,
                        contentDescription = null
                    )
                }
            )
        }

        item {
            val value = if (settings.useCustomRingtoneVolume) {
                (settings.ringtoneVolume * 100).roundToInt().toString() + "%"
            } else {
                stringResource(R.string.disabled)
            }
            ValueGroupRow(
                label = stringResource(R.string.settings_custom_volume),
                value = value,
                supportingContent = {
                    Text(text = stringResource(R.string.settings_custom_volume_desc))
                },
                onClick = onVolumeClick,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = stringResource(R.string.settings_custom_volume)
                    )
                }
            )
        }

        item {
            val value = if (settings.graduallyIncreaseVolume) {
                stringResource(R.string.n_seconds, settings.graduallyIncreaseVolumeDuration)
            } else {
                stringResource(R.string.disabled)
            }
            ValueGroupRow(
                label = stringResource(R.string.settings_gradually_increase_volume),
                value = value,
                supportingContent = {
                    Text(text = stringResource(R.string.settings_gradually_increase_volume_desc))
                },
                onClick = onGraduallyIncreaseVolumeClick,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = stringResource(R.string.settings_gradually_increase_volume)
                    )
                }
            )
        }

        item {
            SwitchGroupRow(
                label = stringResource(R.string.settings_vibration),
                supportingContent = {
                    Text(text = stringResource(R.string.settings_vibration_desc))
                },
                checked = settings.vibrate,
                onCheckedChange = onVibrationChange,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Vibration,
                        contentDescription = stringResource(R.string.settings_vibration)
                    )
                }
            )
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    DeskClockTheme {
        SettingsScreen(
            state = SettingsUiState(
                settings = AppSettingsUiModel(
                    themeMode = ThemeMode.SYSTEM,
                    dynamicColors = true,
                    snoozeDurationMinutes = 10,
                    defaultRingtone = RingtoneItem("Cesium", ""),
                    directBootRingtone = RingtoneItem("Neptunium", ""),
                    useCustomRingtoneVolume = false,
                    ringtoneVolume = 0.5f,
                    vibrate = true,
                    stopwatchShowMilliseconds = true,
                    alarmPopUpStyle = PopUpStyle.DEFAULT,
                    timerPopUpStyle = PopUpStyle.DEFAULT,
                    graduallyIncreaseVolume = false,
                    graduallyIncreaseVolumeDuration = 30
                )
            ),
            onIntent = {},
            onBack = {},
            onNavigateToAlarmStylePicker = {},
            onNavigateToTimerStylePicker = {}
        )
    }
}
