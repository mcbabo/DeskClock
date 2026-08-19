package app.grapheneos.deskclock.stopwatch.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.stopwatch.data.StopwatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StopwatchViewModel(
    private val stopwatchRepository: StopwatchRepository
) : ViewModel() {

    val uiState: StateFlow<StopwatchUiState> = stopwatchRepository.state
        .map { d ->
            if (d.accumulatedMillis == 0L && !d.isRunning && d.laps.isEmpty()) {
                StopwatchUiState.Idle
            } else {
                StopwatchUiState.Active(
                    isRunning = d.isRunning,
                    elapsedMillis = d.accumulatedMillis,
                    laps = d.laps
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StopwatchUiState.Idle
        )

    val elapsedMillis: Flow<Long> = stopwatchRepository.elapsedMillis

    fun handleIntent(intent: StopwatchIntent) {
        when (intent) {
            StopwatchIntent.StartOrResume -> stopwatchRepository.start()
            StopwatchIntent.Pause -> stopwatchRepository.pause()
            StopwatchIntent.Reset -> stopwatchRepository.reset()
            StopwatchIntent.Lap -> stopwatchRepository.lap()
        }
    }
}
