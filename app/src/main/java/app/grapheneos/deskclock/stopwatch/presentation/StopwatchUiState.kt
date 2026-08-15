package app.grapheneos.deskclock.stopwatch.presentation

import androidx.compose.runtime.Immutable
import app.grapheneos.deskclock.stopwatch.data.Lap

/**
 * UI state for the Stopwatch screen.
 */
@Immutable
data class StopwatchUiState(
    val isRunning: Boolean = false,
    val elapsedMillis: Long = 0L,
    val laps: List<Lap> = emptyList()
) {
    val hasLaps: Boolean get() = laps.isNotEmpty()

    val canReset: Boolean get() = !isRunning && elapsedMillis > 0L
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
