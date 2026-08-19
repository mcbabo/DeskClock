package app.grapheneos.deskclock.stopwatch.presentation

import androidx.compose.runtime.Immutable
import app.grapheneos.deskclock.stopwatch.data.Lap

/**
 * UI state for the Stopwatch screen.
 */
@Immutable
sealed interface StopwatchUiState {
    data object Idle : StopwatchUiState

    @Immutable
    data class Active(
        val isRunning: Boolean,
        val elapsedMillis: Long,
        val laps: List<Lap> = emptyList()
    ) : StopwatchUiState {
        val hasLaps: Boolean get() = laps.isNotEmpty()
        val canReset: Boolean get() = !isRunning && elapsedMillis > 0L
    }
}

/**
 * User intents for the Stopwatch screen.
 */
sealed interface StopwatchIntent {
    data object StartOrResume : StopwatchIntent
    data object Pause : StopwatchIntent
    data object Reset : StopwatchIntent
    data object Lap : StopwatchIntent
}
