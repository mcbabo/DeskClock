package app.grapheneos.deskclock.alarm.presentation.popup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.alarm.data.AlarmEntity
import app.grapheneos.deskclock.alarm.data.AlarmRepository
import app.grapheneos.deskclock.alarm.data.AlarmWithInstance
import app.grapheneos.deskclock.alarm.util.AlarmConstants
import app.grapheneos.deskclock.settings.data.AppSettings
import app.grapheneos.deskclock.settings.data.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlarmPopUpViewModel(
    private val alarmRepository: AlarmRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmPopUpUiState())
    val uiState: StateFlow<AlarmPopUpUiState> = _uiState.asStateFlow()

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppSettings()
        )

    private val _effect = Channel<AlarmPopUpEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var currentInstanceId: Long = -1L

    fun handleIntent(intent: AlarmPopUpIntent) {
        when (intent) {
            is AlarmPopUpIntent.Init -> {
                currentInstanceId = intent.instanceId
                if (intent.hour != -1 && intent.minute != -1) {
                    val initialAlarm = AlarmEntity(
                        id = -1, // Temporary
                        hour = intent.hour,
                        minute = intent.minute,
                        daysOfWeek = 0,
                        isEnabled = true,
                        label = intent.label
                    )
                    _uiState.update {
                        it.copy(
                            alarmWithInstance = AlarmWithInstance(initialAlarm, null),
                            isLoading = false
                        )
                    }
                }
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
            val data = alarmRepository.getAlarmByInstanceId(currentInstanceId)
            if (data != null) {
                _uiState.update { it.copy(alarmWithInstance = data, isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun dismissAlarm() {
        viewModelScope.launch {
            if (currentInstanceId != -1L) {
                alarmRepository.dismissAlarm(currentInstanceId)
            }
            _effect.send(AlarmPopUpEffect.FinishAndStopService)
        }
    }

    private fun snoozeAlarm() {
        viewModelScope.launch {
            if (currentInstanceId != -1L) {
                alarmRepository.snoozeAlarm(
                    currentInstanceId,
                    snoozeMinutes = AlarmConstants.DEFAULT_SNOOZE_TIME
                )
            }
            _effect.send(AlarmPopUpEffect.FinishAndStopService)
        }
    }
}
