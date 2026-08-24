package app.grapheneos.deskclock.settings.presentation

import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.exitUntilCollapsedScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.alarm.presentation.components.RingtonePickerDialog
import app.grapheneos.deskclock.alarm.presentation.components.RingtonePickerUiState
import app.grapheneos.deskclock.core.presentation.screenPadding
import app.grapheneos.deskclock.core.ringtone.RingtoneItem
import app.grapheneos.deskclock.core.theme.DeskClockTheme
import app.grapheneos.deskclock.core.util.Constants
import app.grapheneos.deskclock.settings.data.PopUpStyle
import app.grapheneos.deskclock.settings.data.ThemeMode
import app.grapheneos.deskclock.settings.presentation.components.AlarmTimerSettingsSection
import app.grapheneos.deskclock.settings.presentation.components.AppearanceSettingsSection
import app.grapheneos.deskclock.settings.presentation.components.GraduallyIncreaseVolumeDrawer
import app.grapheneos.deskclock.settings.presentation.components.InformationSettingsSection
import app.grapheneos.deskclock.settings.presentation.components.RingtoneVolumeDrawer
import app.grapheneos.deskclock.settings.presentation.components.SnoozeDrawer
import app.grapheneos.deskclock.settings.presentation.components.StopwatchSettingsSection
import app.grapheneos.deskclock.settings.presentation.components.ThemeDrawer

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
    val uriHandler = LocalUriHandler.current
    val scrollBehavior = exitUntilCollapsedScrollBehavior()

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
            AppearanceSettingsSection(
                dynamicColors = settings.dynamicColors,
                themeMode = settings.themeMode,
                alarmPopUpStyle = settings.alarmPopUpStyle,
                timerPopUpStyle = settings.timerPopUpStyle,
                onDynamicColorsChange = { onIntent(SettingsIntent.SetDynamicColors(it)) },
                onThemeClick = { onIntent(SettingsIntent.SetVisibleDialog(SettingsDialog.THEME)) },
                onAlarmPopUpStyleClick = onNavigateToAlarmStylePicker,
                onTimerPopUpStyleClick = onNavigateToTimerStylePicker,
            )

            AlarmTimerSettingsSection(
                settings = settings,
                onSnoozeClick = { onIntent(SettingsIntent.SetVisibleDialog(SettingsDialog.SNOOZE)) },
                onRingtoneClick = { onIntent(SettingsIntent.SetVisibleDialog(SettingsDialog.RINGTONE)) },
                onDirectBootRingtoneClick = {
                    onIntent(
                        SettingsIntent.SetVisibleDialog(SettingsDialog.DIRECT_BOOT_RINGTONE)
                    )
                },
                onVolumeClick = { onIntent(SettingsIntent.SetVisibleDialog(SettingsDialog.VOLUME)) },
                onVibrationChange = { onIntent(SettingsIntent.SetDefaultVibration(it)) },
                onGraduallyIncreaseVolumeClick = {
                    onIntent(SettingsIntent.SetVisibleDialog(SettingsDialog.GRADUALLY_INCREASE_VOLUME))
                }
            )

            StopwatchSettingsSection(
                showStopwatchMilliseconds = settings.stopwatchShowMilliseconds,
                onShowStopwatchMillisecondsChange = {
                    onIntent(SettingsIntent.SetStopwatchShowMilliseconds(it))
                }
            )

            InformationSettingsSection(
                onSourceCodeClick = { uriHandler.openUri(Constants.GITHUB_REPO) }
            )
        }
    }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            onIntent(SettingsIntent.ImportRingtone(it))
        }
    }

    when (state.visibleDialog) {
        SettingsDialog.SNOOZE -> {
            SnoozeDrawer(
                settings.snoozeDurationMinutes,
                onConfirm = {
                    onIntent(SettingsIntent.SetSnoozeTime(it))
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    onIntent(SettingsIntent.SetVisibleDialog(SettingsDialog.NONE))
                }
            ) {
                onIntent(SettingsIntent.SetVisibleDialog(SettingsDialog.NONE))
            }
        }

        SettingsDialog.RINGTONE -> {
            RingtonePickerDialog(
                uiState = RingtonePickerUiState(
                    ringtones = state.ringtones,
                    selectedUri = settings.defaultRingtone.uri,
                    isDirectBootOnly = false
                ),
                onPlayPreview = { onIntent(SettingsIntent.PlayPreview(it)) },
                onStopPreview = { onIntent(SettingsIntent.StopPreview) },
                onImportRingtone = { picker.launch(arrayOf("audio/*")) },
                onDeleteRingtone = { onIntent(SettingsIntent.DeleteCustomRingtone(it)) },
                onDismiss = {
                    onIntent(SettingsIntent.StopPreview)
                    onIntent(SettingsIntent.SetVisibleDialog(SettingsDialog.NONE))
                },
                onConfirm = {
                    onIntent(SettingsIntent.SetDefaultRingtone(it))
                    onIntent(SettingsIntent.SetVisibleDialog(SettingsDialog.NONE))
                }
            )
        }

        SettingsDialog.DIRECT_BOOT_RINGTONE -> {
            RingtonePickerDialog(
                uiState = RingtonePickerUiState(
                    ringtones = state.internalRingtones,
                    selectedUri = settings.directBootRingtone.uri,
                    isDirectBootOnly = true
                ),
                onPlayPreview = { onIntent(SettingsIntent.PlayPreview(it)) },
                onStopPreview = { onIntent(SettingsIntent.StopPreview) },
                onImportRingtone = {},
                onDeleteRingtone = {},
                onDismiss = {
                    onIntent(SettingsIntent.StopPreview)
                    onIntent(SettingsIntent.SetVisibleDialog(SettingsDialog.NONE))
                },
                onConfirm = {
                    onIntent(SettingsIntent.SetDirectBootRingtone(it))
                    onIntent(SettingsIntent.SetVisibleDialog(SettingsDialog.NONE))
                }
            )
        }

        SettingsDialog.VOLUME -> {
            RingtoneVolumeDrawer(
                customRingtoneVolumeEnabled = settings.useCustomRingtoneVolume,
                currentVolume = settings.ringtoneVolume,
                onChange = { enabled, volume ->
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    onIntent(SettingsIntent.SetCustomRingtoneVolumeEnabled(enabled))
                    onIntent(SettingsIntent.SetCustomRingtoneVolume(volume))
                    onIntent(SettingsIntent.SetVisibleDialog(SettingsDialog.NONE))
                }
            ) {
                onIntent(SettingsIntent.SetVisibleDialog(SettingsDialog.NONE))
            }
        }

        SettingsDialog.THEME -> {
            ThemeDrawer(
                settings.themeMode,
                onThemeChange = {
                    onIntent(SettingsIntent.UpdateTheme(it))
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    onIntent(SettingsIntent.SetVisibleDialog(SettingsDialog.NONE))
                },
                onDismissRequest = { onIntent(SettingsIntent.SetVisibleDialog(SettingsDialog.NONE)) }
            )
        }

        SettingsDialog.GRADUALLY_INCREASE_VOLUME -> {
            GraduallyIncreaseVolumeDrawer(
                initiallyEnabled = settings.graduallyIncreaseVolume,
                initialDuration = settings.graduallyIncreaseVolumeDuration,
                onConfirm = { enabled, duration ->
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    onIntent(SettingsIntent.SetGraduallyIncreaseVolume(enabled))
                    onIntent(SettingsIntent.SetGraduallyIncreaseVolumeDuration(duration))
                    onIntent(SettingsIntent.SetVisibleDialog(SettingsDialog.NONE))
                },
                onDismiss = { onIntent(SettingsIntent.SetVisibleDialog(SettingsDialog.NONE)) }
            )
        }

        SettingsDialog.NONE -> {}
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
                    defaultRingtone = RingtoneItem("Cesium", "".toUri()),
                    directBootRingtone = RingtoneItem("Neptunium", "".toUri()),
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
