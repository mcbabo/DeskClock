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
import androidx.compose.runtime.collectAsState
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
import app.grapheneos.deskclock.alarm.presentation.RingtoneItem
import app.grapheneos.deskclock.alarm.presentation.components.RingtonePickerDialog
import app.grapheneos.deskclock.core.navigation.LocalNavBackStack
import app.grapheneos.deskclock.core.navigation.Route
import app.grapheneos.deskclock.core.presentation.components.groupitems.ListGroup
import app.grapheneos.deskclock.core.presentation.components.groupitems.SwitchGroupRow
import app.grapheneos.deskclock.core.presentation.components.groupitems.ValueGroupRow
import app.grapheneos.deskclock.core.presentation.screenPadding
import app.grapheneos.deskclock.core.theme.DeskClockTheme
import app.grapheneos.deskclock.settings.data.PopUpStyle
import app.grapheneos.deskclock.settings.data.ThemeMode
import app.grapheneos.deskclock.settings.presentation.components.RingtoneVolumeDrawer
import app.grapheneos.deskclock.settings.presentation.components.SnoozeDrawer
import app.grapheneos.deskclock.settings.presentation.components.ThemeDrawer
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val backStack = LocalNavBackStack.current

    SettingsContent(
        state = state,
        onIntent = { intent -> viewModel.handleIntent(intent) },
        onBack = onBack,
        onNavigateToAlarmStyle = { backStack.add(Route.AlarmStylePicker) },
        onNavigateToTimerStyle = { backStack.add(Route.TimerStylePicker) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
    onNavigateToAlarmStyle: () -> Unit,
    onNavigateToTimerStyle: () -> Unit
) {
    val view = LocalView.current

    val scrollBehavior = exitUntilCollapsedScrollBehavior()
    var showSnoozeDialog by remember { mutableStateOf(false) }
    var showRingtoneDialog by remember { mutableStateOf(false) }
    var showRingtoneVolumeDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val settings = state.settings ?: return

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
            ListGroup(title = stringResource(R.string.general)) {
                item {
                    SwitchGroupRow(
                        label = stringResource(R.string.settings_dynamic_colors),
                        supportingContent = {
                            Text(text = stringResource(R.string.settings_dynamic_colors_desc))
                        },
                        checked = settings.dynamicColors,
                        onCheckedChange = { newChecked ->
                            onIntent(SettingsIntent.SetDynamicColors(newChecked))
                        },
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
                        value = stringResource(settings.themeMode.displayNameRes),
                        supportingContent = {
                            Text(text = stringResource(R.string.setting_theme_desc))
                        },
                        onClick = { showThemeDialog = true },
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
                        value = stringResource(settings.alarmPopUpStyle.titleRes),
                        supportingContent = {
                            Text(text = stringResource(R.string.settings_alarm_popup_style_desc))
                        },
                        onClick = onNavigateToAlarmStyle,
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
                        value = stringResource(settings.timerPopUpStyle.titleRes),
                        supportingContent = {
                            Text(text = stringResource(R.string.settings_timer_popup_style_desc))
                        },
                        onClick = onNavigateToTimerStyle,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Palette,
                                contentDescription = null
                            )
                        }
                    )
                }
            }

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
                        onClick = { showSnoozeDialog = true },
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
                        onClick = { showRingtoneDialog = true },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.MusicNote,
                                contentDescription = stringResource(R.string.settings_alarm_sound)
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
                        onClick = { showRingtoneVolumeDialog = true },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Tune,
                                contentDescription = stringResource(R.string.settings_alarm_sound)
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
                        onCheckedChange = { newChecked ->
                            onIntent(SettingsIntent.SetDefaultVibration(newChecked))
                        },
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
}

@Preview
@Composable
fun SettingsScreenPreview() {
    DeskClockTheme {
        SettingsContent(
            state = SettingsUiState(
                settings = AppSettingsUiModel(
                    themeMode = ThemeMode.SYSTEM,
                    dynamicColors = true,
                    snoozeDurationMinutes = 10,
                    defaultRingtone = RingtoneItem("Cesium", ""),
                    useCustomRingtoneVolume = false,
                    ringtoneVolume = 0.5f,
                    vibrate = true,
                    stopwatchShowMilliseconds = true,
                    alarmPopUpStyle = PopUpStyle.DEFAULT,
                    timerPopUpStyle = PopUpStyle.DEFAULT
                )
            ),
            onIntent = {},
            onBack = {},
            onNavigateToAlarmStyle = {},
            onNavigateToTimerStyle = {}
        )
    }
}
