package app.grapheneos.deskclock.timer.presentation.popup

import androidx.compose.runtime.Immutable

@Immutable
data class TimerPopUpUiState(
    val remainingTime: Long = 0L
)

sealed interface TimerPopUpIntent {
    data object Stop : TimerPopUpIntent
}

sealed interface TimerPopUpEffect {
    data object Finish : TimerPopUpEffect
}
