package app.grapheneos.deskclock.settings.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.grapheneos.deskclock.BuildConfig
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.core.presentation.components.groupitems.ListGroup
import app.grapheneos.deskclock.core.presentation.components.groupitems.SwitchGroupRow
import app.grapheneos.deskclock.core.presentation.components.groupitems.ValueGroupRow
import app.grapheneos.deskclock.settings.data.PopUpStyle
import app.grapheneos.deskclock.settings.data.ThemeMode
import app.grapheneos.deskclock.settings.presentation.AppSettingsUiModel
import app.grapheneos.deskclock.settings.presentation.getDisplayNameRes
import app.grapheneos.deskclock.settings.presentation.getTitleRes
import kotlin.math.roundToInt

@Composable
fun InformationSettingsSection(
    onSourceCodeClick: () -> Unit,
) {
    ListGroup(title = stringResource(R.string.settings_information)) {
        item {
            ValueGroupRow(
                label = stringResource(R.string.settings_source_code),
                value = "GitHub",
                supportingContent = {
                    Text(text = stringResource(R.string.settings_source_code_desc))
                },
                onClick = onSourceCodeClick,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Code,
                        contentDescription = stringResource(R.string.settings_source_code)
                    )
                }
            )
        }

        item {
            ValueGroupRow(
                label = stringResource(R.string.settings_app_version),
                value = BuildConfig.VERSION_NAME,
                supportingContent = {
                    Text(text = stringResource(R.string.settings_app_version_desc))
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = stringResource(R.string.settings_app_version)
                    )
                }
            )
        }
    }
}

@Composable
fun AppearanceSettingsSection(
    dynamicColors: Boolean,
    themeMode: ThemeMode,
    alarmPopUpStyle: PopUpStyle,
    timerPopUpStyle: PopUpStyle,
    onDynamicColorsChange: (Boolean) -> Unit,
    onThemeClick: () -> Unit,
    onAlarmPopUpStyleClick: () -> Unit,
    onTimerPopUpStyleClick: () -> Unit,
) {
    ListGroup(title = stringResource(R.string.settings_appearance)) {
        item {
            ValueGroupRow(
                label = stringResource(R.string.setting_theme),
                value = stringResource(themeMode.getDisplayNameRes()),
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
                label = stringResource(R.string.settings_alarm_popup_style),
                value = stringResource(alarmPopUpStyle.getTitleRes()),
                supportingContent = {
                    Text(text = stringResource(R.string.settings_alarm_popup_style_desc))
                },
                onClick = onAlarmPopUpStyleClick,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = stringResource(R.string.settings_alarm_popup_style)
                    )
                }
            )
        }

        item {
            ValueGroupRow(
                label = stringResource(R.string.settings_timer_popup_style),
                value = stringResource(timerPopUpStyle.getTitleRes()),
                supportingContent = {
                    Text(text = stringResource(R.string.settings_timer_popup_style_desc))
                },
                onClick = onTimerPopUpStyleClick,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = stringResource(R.string.settings_timer_popup_style)
                    )
                }
            )
        }
    }
}

@Composable
fun StopwatchSettingsSection(
    showStopwatchMilliseconds: Boolean,
    onShowStopwatchMillisecondsChange: (Boolean) -> Unit
) {
    ListGroup(title = stringResource(R.string.settings_stopwatch)) {
        item {
            SwitchGroupRow(
                label = stringResource(R.string.settings_stopwatch_milliseconds),
                supportingContent = {
                    Text(text = stringResource(R.string.settings_stopwatch_milliseconds_desc))
                },
                checked = showStopwatchMilliseconds,
                onCheckedChange = onShowStopwatchMillisecondsChange,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = stringResource(R.string.settings_stopwatch_milliseconds)
                    )
                }
            )
        }
    }
}

@Composable
fun AlarmTimerSettingsSection(
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
                label = stringResource(R.string.settings_alarm_sound),
                value = settings.defaultRingtone.name.let { name ->
                    if (name.length > 15) name.take(15).plus("...") else name
                },
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
                        contentDescription = stringResource(R.string.settings_direct_boot_sound)
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
    }
}
