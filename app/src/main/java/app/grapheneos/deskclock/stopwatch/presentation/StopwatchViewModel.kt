package app.grapheneos.deskclock.stopwatch.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.settings.data.SettingsRepository
import app.grapheneos.deskclock.stopwatch.data.StopwatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class StopwatchViewModel(
    private val stopwatchRepository: StopwatchRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<StopwatchUiState> = combine(
        stopwatchRepository.state,
        settingsRepository.settings
    ) { d, settings ->
        StopwatchUiState(
            isRunning = d.isRunning,
            elapsedMillis = d.accumulatedMillis,
            laps = d.laps,
            showMilliseconds = settings.stopwatchShowMilliseconds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StopwatchUiState()
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
