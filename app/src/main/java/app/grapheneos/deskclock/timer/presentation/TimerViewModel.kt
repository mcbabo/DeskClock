package app.grapheneos.deskclock.timer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.timer.data.TimerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TimerViewModel(
    private val timerRepository: TimerRepository
) : ViewModel() {

    val uiState: StateFlow<TimerUiState> = timerRepository.state
        .map { data ->
            TimerUiState(
                inputTime = data.inputTime,
                totalTime = data.totalMillis,
                isRunning = data.isRunning,
                isStarted = data.isStarted,
                isFinished = data.isFinished
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TimerUiState()
        )

    val remainingMillis: Flow<Long> = timerRepository.remainingMillis

    fun handleIntent(intent: TimerIntent) {
        viewModelScope.launch {
            when (intent) {
                is TimerIntent.EnterDigit -> timerRepository.enterDigit(intent.digit)
                TimerIntent.Backspace -> timerRepository.backspace()
                TimerIntent.Start -> timerRepository.start()
                TimerIntent.TogglePauseResume -> timerRepository.togglePauseResume()
                TimerIntent.Reset -> timerRepository.reset()
            }
        }
    }
}
