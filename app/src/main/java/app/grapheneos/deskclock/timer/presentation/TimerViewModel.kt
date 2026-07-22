package app.grapheneos.deskclock.timer.presentation

import androidx.lifecycle.ViewModel
import app.grapheneos.deskclock.timer.data.TimerRepository
import kotlinx.coroutines.flow.StateFlow

class TimerViewModel(
    private val repository: TimerRepository
) : ViewModel() {

    val uiState: StateFlow<TimerUiState> = repository.timerState

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
