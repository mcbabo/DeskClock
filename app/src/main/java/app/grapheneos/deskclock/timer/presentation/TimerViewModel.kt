package app.grapheneos.deskclock.timer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.timer.data.TimerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class TimerViewModel(
    private val repository: TimerRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TimerUiState> = combine(
        repository.state,
        repository.remainingMillis
    ) { data, remaining ->
        TimerUiState(
            remainingTime = remaining,
            totalTime = data.totalMillis,
            inputTime = data.inputTime,
            isStarted = data.isStarted,
            isRunning = data.isRunning,
            isFinished = data.isFinished,
            progress = data.progress
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TimerUiState()
    )

    fun handleIntent(intent: TimerIntent) {
        when (intent) {
            is TimerIntent.EnterDigit -> repository.enterDigit(intent.digit)
            TimerIntent.Backspace -> repository.backspace()
            TimerIntent.Start -> repository.start()
            TimerIntent.TogglePauseResume -> repository.togglePauseResume()
            TimerIntent.Reset -> repository.reset()
        }
    }
}
