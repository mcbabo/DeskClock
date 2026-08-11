package app.grapheneos.deskclock.settings.presentation.style

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.alarm.presentation.AlarmUiModel
import app.grapheneos.deskclock.alarm.presentation.popup.getAlarmLabel
import app.grapheneos.deskclock.alarm.presentation.popup.styles.AlarmPopUpDefault
import app.grapheneos.deskclock.alarm.presentation.popup.styles.AlarmPopUpTertiary
import app.grapheneos.deskclock.alarm.presentation.popup.styles.AlarmPopUpVariant
import app.grapheneos.deskclock.settings.data.PopUpStyle
import app.grapheneos.deskclock.settings.presentation.SettingsIntent
import app.grapheneos.deskclock.settings.presentation.SettingsUiState
import app.grapheneos.deskclock.timer.presentation.popup.styles.TimerPopUpDefault
import app.grapheneos.deskclock.timer.presentation.popup.styles.TimerPopUpTertiary
import app.grapheneos.deskclock.timer.presentation.popup.styles.TimerPopUpVariant

@Composable
fun AlarmStylePickerScreen(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit
) {
    val currentStyle = state.settings?.alarmPopUpStyle ?: PopUpStyle.DEFAULT

    val mockAlarm = AlarmUiModel(
        id = -1,
        hour = 7,
        minute = 30,
        daysOfWeek = 0,
        isEnabled = true,
        deleteAfterUse = false,
        label = stringResource(R.string.wake_up),
        ringtoneUri = "",
        vibrate = true,
        snoozeDurationMinutes = 10,
        timeText = "7:30",
        daysOfWeekText = stringResource(R.string.one_time)
    )

    val labelText = getAlarmLabel(mockAlarm)

    StylePickerScreen(
        title = stringResource(R.string.settings_alarm_popup_style),
        currentStyle = currentStyle,
        onStyleSelected = { onIntent(SettingsIntent.SetAlarmPopUpStyle(it)) },
        onBack = onBack,
        previewContent = { style ->
            when (style) {
                PopUpStyle.DEFAULT -> AlarmPopUpDefault(
                    alarm = mockAlarm,
                    labelText = labelText,
                    onDismiss = {},
                    onSnooze = {}
                )

                PopUpStyle.VARIANT -> AlarmPopUpVariant(
                    alarm = mockAlarm,
                    labelText = labelText,
                    onDismiss = {},
                    onSnooze = {}
                )

                PopUpStyle.TERTIARY -> AlarmPopUpTertiary(
                    alarm = mockAlarm,
                    labelText = labelText,
                    onDismiss = {},
                    onSnooze = {}
                )
            }
        }
    )
}

@Composable
fun TimerStylePickerScreen(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit
) {
    val currentStyle = state.settings?.timerPopUpStyle ?: PopUpStyle.DEFAULT

    StylePickerScreen(
        title = stringResource(R.string.settings_timer_popup_style),
        currentStyle = currentStyle,
        onStyleSelected = { onIntent(SettingsIntent.SetTimerPopUpStyle(it)) },
        onBack = onBack,
        previewContent = { style ->
            when (style) {
                PopUpStyle.DEFAULT -> TimerPopUpDefault(
                    remainingTime = 0L,
                    onStop = {},
                    onAddMinute = {}
                )

                PopUpStyle.VARIANT -> TimerPopUpVariant(
                    remainingTime = 0L,
                    onStop = {},
                    onAddMinute = {}
                )

                PopUpStyle.TERTIARY -> TimerPopUpTertiary(
                    remainingTime = 0L,
                    onStop = {},
                    onAddMinute = {}
                )
            }
        }
    )
}
