package app.grapheneos.deskclock.timer.presentation.popup

import androidx.compose.runtime.Composable
import app.grapheneos.deskclock.settings.data.PopUpStyle
import app.grapheneos.deskclock.timer.presentation.popup.styles.TimerPopUpDefault
import app.grapheneos.deskclock.timer.presentation.popup.styles.TimerPopUpVariant

@Composable
fun TimerPopUpScreen(
    uiState: TimerPopUpUiState,
    onIntent: (TimerPopUpIntent) -> Unit
) {
    when (uiState.style) {
        PopUpStyle.DEFAULT -> {
            TimerPopUpDefault(
                remainingTime = uiState.remainingTime,
                onStop = { onIntent(TimerPopUpIntent.Stop) }
            )
        }

        PopUpStyle.VARIANT -> {
            TimerPopUpVariant(
                remainingTime = uiState.remainingTime,
                onStop = { onIntent(TimerPopUpIntent.Stop) }
            )
        }
    }
}
