package app.grapheneos.deskclock.stopwatch.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.stopwatch.data.StopwatchData
import app.grapheneos.deskclock.stopwatch.data.StopwatchRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.milliseconds

class StopwatchViewModel(
    private val repository: StopwatchRepository
) : ViewModel() {

    private val data: StateFlow<StopwatchData> = repository.state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StopwatchData()
        )

    val uiState: StateFlow<StopwatchUiState> = data
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val elapsedMillis: Flow<Long> = data.flatMapLatest { d ->
        if (d.isRunning) {
            flow {
                emit(d.getElapsedMillis())
                while (true) {
                    delay(TICK_INTERVAL_MILLIS.milliseconds)
                    emit(d.getElapsedMillis())
                }
            }
        } else {
            flowOf(d.accumulatedMillis)
        }
    }

    fun handleIntent(intent: StopwatchIntent) {
        when (intent) {
            StopwatchIntent.StartOrResume -> repository.start()
            StopwatchIntent.Pause -> repository.pause()
            StopwatchIntent.Reset -> repository.reset()
            StopwatchIntent.Lap -> repository.lap()
        }
    }

    companion object {
        private const val TICK_INTERVAL_MILLIS = 100L
    }
}
