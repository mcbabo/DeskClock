package app.grapheneos.deskclock.timer.presentation.popup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.timer.data.TimerRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TimerPopUpViewModel(
    private val repository: TimerRepository
) : ViewModel() {
    val uiState: StateFlow<TimerPopUpUiState> = repository.timerState
        .map { timerState ->
            TimerPopUpUiState(remainingTime = timerState.remainingTime)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TimerPopUpUiState()
        )

    private val _effect = Channel<TimerPopUpEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun handleIntent(action: TimerPopUpIntent) {
        when (action) {
            TimerPopUpIntent.Stop -> {
                viewModelScope.launch {
                    repository.reset()
                    _effect.send(TimerPopUpEffect.Finish)
                }
            }
        }
    }
}
