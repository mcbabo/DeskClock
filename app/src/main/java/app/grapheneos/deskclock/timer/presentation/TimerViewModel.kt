package app.grapheneos.deskclock.timer.presentation

import androidx.lifecycle.ViewModel
import app.grapheneos.deskclock.timer.data.TimerRepository
import kotlinx.coroutines.flow.StateFlow

class TimerViewModel(
    private val repository: TimerRepository
) : ViewModel() {

    val uiState: StateFlow<TimerUiState> = repository.timerState

    fun handleAction(intent: TimerAction) {
        when (intent) {
            is TimerAction.EnterDigit -> repository.enterDigit(intent.digit)
            TimerAction.Backspace -> repository.backspace()
            TimerAction.Start -> repository.start()
            TimerAction.TogglePauseResume -> repository.togglePauseResume()
            TimerAction.Reset -> repository.reset()
        }
    }
}
