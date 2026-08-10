package app.grapheneos.deskclock.timer.presentation.popup

import androidx.compose.runtime.Immutable
import app.grapheneos.deskclock.settings.data.PopUpStyle

@Immutable
data class TimerPopUpUiState(
    val remainingTime: Long = 0L,
    val style: PopUpStyle = PopUpStyle.DEFAULT
)

sealed interface TimerPopUpIntent {
    data object Stop : TimerPopUpIntent
    data object AddMinute : TimerPopUpIntent
}

sealed interface TimerPopUpEffect {
    data object Finish : TimerPopUpEffect
}
