package app.grapheneos.deskclock.timer.presentation

import androidx.compose.runtime.Immutable

/**
 * UI state for the Timer screen.
 */
@Immutable
data class TimerUiState(
    val inputTime: String = "000000",
    val totalTime: Long = 0L,
    val isRunning: Boolean = false,
    val isStarted: Boolean = false,
    val isFinished: Boolean = false
) {
    val canStart: Boolean get() = inputTime.replace("0", "").isNotEmpty()
}

/**
 * User intents for the Timer screen.
 */
sealed interface TimerIntent {
    data class EnterDigit(val digit: Int) : TimerIntent
    data object Backspace : TimerIntent
    data object Start : TimerIntent
    data object TogglePauseResume : TimerIntent
    data object Reset : TimerIntent
}
