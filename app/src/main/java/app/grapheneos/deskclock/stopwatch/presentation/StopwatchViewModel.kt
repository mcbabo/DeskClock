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
    private val repository: StopwatchRepository
) : ViewModel() {

    val uiState: StateFlow<StopwatchUiState> = repository.state
        .map { d ->
            StopwatchUiState(
                isRunning = d.isRunning,
                elapsedMillis = d.accumulatedMillis,
                laps = d.laps
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StopwatchUiState()
        )

    val elapsedMillis: Flow<Long> = repository.elapsedMillis

    fun handleIntent(intent: StopwatchIntent) {
        when (intent) {
            StopwatchIntent.StartOrResume -> repository.start()
            StopwatchIntent.Pause -> repository.pause()
            StopwatchIntent.Reset -> repository.reset()
            StopwatchIntent.Lap -> repository.lap()
        }
    }
}
