package app.grapheneos.deskclock.timer.presentation.popup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.settings.data.AppSettings
import app.grapheneos.deskclock.settings.data.SettingsRepository
import app.grapheneos.deskclock.timer.data.TimerRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TimerPopUpViewModel(
    private val timerRepository: TimerRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {
    val uiState: StateFlow<TimerPopUpUiState> = combine(
        timerRepository.state,
        timerRepository.remainingMillis
    ) { _, remaining ->
        TimerPopUpUiState(remainingTime = remaining)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TimerPopUpUiState()
    )

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppSettings()
        )

    private val _effect = Channel<TimerPopUpEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun handleIntent(action: TimerPopUpIntent) {
        when (action) {
            TimerPopUpIntent.Stop -> {
                viewModelScope.launch {
                    timerRepository.reset()
                    _effect.send(TimerPopUpEffect.Finish)
                }
            }
        }
    }
}
