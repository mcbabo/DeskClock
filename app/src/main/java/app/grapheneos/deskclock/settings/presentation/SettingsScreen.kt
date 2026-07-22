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
import androidx.compose.material.icons.outlined.Snooze
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
import app.grapheneos.deskclock.alarm.presentation.components.RingtonePickerDialog
import app.grapheneos.deskclock.core.presentation.components.groupitems.ListGroup
import app.grapheneos.deskclock.core.presentation.components.groupitems.SwitchGroupRow
import app.grapheneos.deskclock.core.presentation.components.groupitems.ValueGroupRow
import app.grapheneos.deskclock.core.presentation.screenPadding
import app.grapheneos.deskclock.settings.presentation.components.SnoozeDrawer
import app.grapheneos.deskclock.settings.presentation.components.ThemeDrawer
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    SettingsContent(
        state = state,
        onIntent = { intent -> viewModel.handleIntent(intent) },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit
) {
    val view = LocalView.current

    val scrollBehavior = exitUntilCollapsedScrollBehavior()
    var showSnoozeDialog by remember { mutableStateOf(false) }
    var showRingtoneDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

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
                        checked = state.settings.dynamicColors,
                        onCheckedChange = { newChecked ->
                            onIntent(SettingsIntent.SetDynamicColors(newChecked))
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.ColorLens, contentDescription = null)
                        }
                    )
                }

                item(onClick = { showThemeDialog = true }) {
                    ValueGroupRow(
                        label = stringResource(R.string.setting_theme),
                        value = state.settings.themeMode.displayName,
                        supportingContent = {
                            Text(text = stringResource(R.string.setting_theme_desc))
                        },
                        onClick = { showThemeDialog = true },
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.Colorize, contentDescription = null)
                        }
                    )
                }
            }

            ListGroup(title = stringResource(R.string.settings_alarm_and_timer)) {
                item(onClick = { showSnoozeDialog = true }) {
                    ValueGroupRow(
                        label = stringResource(R.string.settings_snooze_duration),
                        value = stringResource(
                            R.string.n_minutes,
                            state.settings.snoozeDurationMinutes
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

                item(onClick = { showRingtoneDialog = true }) {
                    ValueGroupRow(
                        label = stringResource(R.string.settings_alarm_sound),
                        value = state.settings.defaultRingtone.name,
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
                    SwitchGroupRow(
                        label = stringResource(R.string.settings_vibration),
                        supportingContent = {
                            Text(text = stringResource(R.string.settings_vibration_desc))
                        },
                        checked = state.settings.vibrate,
                        onCheckedChange = { newChecked ->
                            onIntent(SettingsIntent.SetDefaultVibration(newChecked))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Vibration,
                                stringResource(R.string.settings_vibration)
                            )
                        }
                    )
                }
            }
        }
    }

    if (showSnoozeDialog) {
        SnoozeDrawer(
            state.settings.snoozeDurationMinutes,
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
            initialUri = state.settings.defaultRingtone.uri,
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

    if (showThemeDialog) {
        ThemeDrawer(
            state.settings.themeMode,
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
    SettingsContent(
        state = SettingsUiState(),
        onIntent = {},
        onBack = {}
    )
}
