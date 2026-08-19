package app.grapheneos.deskclock.timer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.timer.data.TimerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TimerViewModel(
    private val timerRepository: TimerRepository
) : ViewModel() {

    val uiState: StateFlow<TimerUiState> = timerRepository.state
        .map { data ->
            if (data.isStarted || data.isFinished) {
                TimerUiState.Active(
                    totalTime = data.totalMillis,
                    isRunning = data.isRunning,
                    isFinished = data.isFinished,
                    progress = data.progress
                )
            } else {
                TimerUiState.Idle(
                    inputTime = data.inputTime
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TimerUiState.Idle()
        )

    val remainingMillis: Flow<Long> = timerRepository.remainingMillis

    fun handleIntent(intent: TimerIntent) {
        when (intent) {
            is TimerIntent.EnterDigit -> timerRepository.enterDigit(intent.digit)
            TimerIntent.Backspace -> timerRepository.backspace()
            TimerIntent.Start -> timerRepository.start()
            TimerIntent.TogglePauseResume -> timerRepository.togglePauseResume()
            TimerIntent.Reset -> timerRepository.reset()
        }
    }
}
