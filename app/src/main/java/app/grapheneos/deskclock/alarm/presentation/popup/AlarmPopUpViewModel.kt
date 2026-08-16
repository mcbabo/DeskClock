package app.grapheneos.deskclock.alarm.presentation.popup

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.alarm.data.AlarmRepository
import app.grapheneos.deskclock.alarm.presentation.AlarmUiModel
import app.grapheneos.deskclock.alarm.presentation.toUiModel
import app.grapheneos.deskclock.alarm.util.AlarmDayFormatter.formatDaysOfWeek
import app.grapheneos.deskclock.core.util.Constants
import app.grapheneos.deskclock.core.util.formatSystemTime
import app.grapheneos.deskclock.settings.data.SettingsRepository
import app.grapheneos.deskclock.settings.presentation.AppSettingsUiModel
import app.grapheneos.deskclock.settings.presentation.toUiModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlarmPopUpViewModel(
    private val application: Application,
    private val alarmRepository: AlarmRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmPopUpUiState())
    val uiState: StateFlow<AlarmPopUpUiState> = _uiState.asStateFlow()

    val settings: StateFlow<AppSettingsUiModel?> = settingsRepository.settings
        .map { it.toUiModel() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    private val _effect = Channel<AlarmPopUpEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var currentInstanceId: Long = -1L

    init {
        settingsRepository.settings
            .onEach { settings ->
                _uiState.update { it.copy(style = settings.alarmPopUpStyle) }
            }
            .launchIn(viewModelScope)
    }

    fun handleIntent(intent: AlarmPopUpIntent) {
        when (intent) {
            is AlarmPopUpIntent.Init -> {
                currentInstanceId = intent.instanceId
                if (intent.hour != -1 && intent.minute != -1) {
                    val initialAlarm = AlarmUiModel(
                        id = -1, // Temporary
                        hour = intent.hour,
                        minute = intent.minute,
                        daysOfWeek = 0,
                        isEnabled = true,
                        deleteAfterUse = false,
                        label = intent.label,
                        ringtoneUri = Uri.EMPTY,
                        vibrate = true,
                        snoozeDurationMinutes = 10,
                        timeText = formatSystemTime(application, intent.hour, intent.minute),
                        daysOfWeekText = formatDaysOfWeek(application, 0)
                    )
                    _uiState.update {
                        it.copy(
                            alarm = initialAlarm,
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
                _uiState.update { it.copy(alarm = data.toUiModel(application), isLoading = false) }
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
                    snoozeMinutes = Constants.Alarm.DEFAULT_SNOOZE_TIME
                )
            }
            _effect.send(AlarmPopUpEffect.FinishAndStopService)
        }
    }
}
