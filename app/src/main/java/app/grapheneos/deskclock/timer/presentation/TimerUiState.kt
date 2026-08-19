package app.grapheneos.deskclock.timer.presentation

import androidx.compose.runtime.Immutable

/**
 * UI state for the Timer screen.
 */
@Immutable
sealed interface TimerUiState {
    data class Idle(
        val inputTime: String = "000000"
    ) : TimerUiState {
        val canStart: Boolean get() = inputTime.toLong() > 0
    }

    data class Active(
        val totalTime: Long,
        val isRunning: Boolean,
        val isFinished: Boolean,
        val progress: Float
    ) : TimerUiState
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
