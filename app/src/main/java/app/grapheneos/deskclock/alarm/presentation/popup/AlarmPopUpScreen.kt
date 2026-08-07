package app.grapheneos.deskclock.alarm.presentation.popup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.alarm.presentation.AlarmUiModel
import app.grapheneos.deskclock.alarm.presentation.popup.styles.AlarmPopUpDefault
import app.grapheneos.deskclock.alarm.presentation.popup.styles.AlarmPopUpVariant
import app.grapheneos.deskclock.settings.data.PopUpStyle

@Composable
fun AlarmPopUpScreen(
    uiState: AlarmPopUpUiState,
    onIntent: (AlarmPopUpIntent) -> Unit
) {
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val labelText = getAlarmLabel(uiState.alarm)
        when (uiState.style) {
            PopUpStyle.DEFAULT -> {
                AlarmPopUpDefault(
                    alarm = uiState.alarm,
                    labelText = labelText,
                    onDismiss = { onIntent(AlarmPopUpIntent.Dismiss) },
                    onSnooze = { onIntent(AlarmPopUpIntent.Snooze) }
                )
            }

            PopUpStyle.VARIANT -> {
                AlarmPopUpVariant(
                    alarm = uiState.alarm,
                    labelText = labelText,
                    onDismiss = { onIntent(AlarmPopUpIntent.Dismiss) },
                    onSnooze = { onIntent(AlarmPopUpIntent.Snooze) }
                )
            }
        }
    }
}

@Composable
fun getAlarmLabel(alarm: AlarmUiModel?): String {
    return when {
        alarm == null -> stringResource(R.string.alarm)
        alarm.label.isNotEmpty() -> alarm.label
        else -> stringResource(R.string.wake_up)
    }
}
