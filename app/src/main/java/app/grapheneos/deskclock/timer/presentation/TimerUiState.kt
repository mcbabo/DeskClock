package app.grapheneos.deskclock.timer.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class TimerUiState(
    val remainingTime: Long = 0L,
    val totalTime: Long = 0L,
    val inputTime: String = "000000", // HHMMSS
    val isStarted: Boolean = false,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    val progress: Float = 0f
)

sealed interface TimerIntent {
    data class EnterDigit(val digit: Int) : TimerIntent
    data object Backspace : TimerIntent
    data object Start : TimerIntent
    data object TogglePauseResume : TimerIntent
    data object Reset : TimerIntent
}
