package app.grapheneos.deskclock.alarm.presentation.popup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.alarm.data.AlarmRepository
import app.grapheneos.deskclock.alarm.util.AlarmConstants
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlarmPopUpViewModel(
    private val repository: AlarmRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmPopUpUiState())
    val uiState: StateFlow<AlarmPopUpUiState> = _uiState.asStateFlow()

    private val _effect = Channel<AlarmPopUpEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var currentInstanceId: Long = -1L

    fun handleIntent(intent: AlarmPopUpIntent) {
        when (intent) {
            is AlarmPopUpIntent.Init -> {
                currentInstanceId = intent.instanceId
                loadAlarmData()
            }

            AlarmPopUpIntent.Dismiss -> dismissAlarm()
            AlarmPopUpIntent.Snooze -> snoozeAlarm()
        }
    }

    private fun loadAlarmData() {
        if (currentInstanceId == -1L) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        viewModelScope.launch {
            val data = repository.getAlarmByInstanceId(currentInstanceId)
            _uiState.update { it.copy(alarmWithInstance = data, isLoading = false) }
        }
    }

    private fun dismissAlarm() {
        viewModelScope.launch {
            if (currentInstanceId != -1L) {
                repository.dismissAlarm(currentInstanceId)
            }
            _effect.send(AlarmPopUpEffect.FinishAndStopService)
        }
    }

    private fun snoozeAlarm() {
        viewModelScope.launch {
            if (currentInstanceId != -1L) {
                repository.snoozeAlarm(
                    currentInstanceId,
                    snoozeMinutes = AlarmConstants.DEFAULT_SNOOZE_TIME
                )
            }
            _effect.send(AlarmPopUpEffect.FinishAndStopService)
        }
    }
}
