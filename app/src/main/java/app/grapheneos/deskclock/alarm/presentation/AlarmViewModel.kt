package app.grapheneos.deskclock.alarm.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.alarm.data.AlarmEntity
import app.grapheneos.deskclock.alarm.data.AlarmRepository
import app.grapheneos.deskclock.core.audio.AudioPlayer
import app.grapheneos.deskclock.core.ringtone.RingtoneRepository
import app.grapheneos.deskclock.settings.data.AppSettings
import app.grapheneos.deskclock.settings.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlarmViewModel(
    private val alarmRepository: AlarmRepository,
    private val settingsRepository: SettingsRepository,
    private val ringtoneRepository: RingtoneRepository,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    private val settings = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppSettings()
        )

    init {
        observeAlarms()
        handleIntent(AlarmIntent.LoadSystemRingtones)
    }

    fun handleIntent(intent: AlarmIntent) {
        when (intent) {
            is AlarmIntent.LoadAlarms -> {} // Handled in init
            is AlarmIntent.ToggleAlarm -> viewModelScope.launch { alarmRepository.toggleAlarm(intent.alarm) }
            is AlarmIntent.UpdateAlarm -> viewModelScope.launch { alarmRepository.updateAlarm(intent.alarm) }
            is AlarmIntent.DeleteAlarm -> viewModelScope.launch { alarmRepository.deleteAlarm(intent.alarm) }
            is AlarmIntent.RestoreAlarm -> viewModelScope.launch { alarmRepository.addAlarm(intent.alarm) }
            is AlarmIntent.AddAlarm -> viewModelScope.launch {
                val settings = settings.value

                val alarm = AlarmEntity(
                    hour = intent.hour,
                    minute = intent.minute,
                    daysOfWeek = intent.daysOfWeek,
                    isEnabled = true,
                    deleteAfterUse = intent.deleteAfterUse,
                    label = intent.label,
                    ringtoneUri = settings.defaultRingtone.uri,
                    vibrate = settings.vibrate,
                    snoozeDurationMinutes = settings.snoozeDurationMinutes
                )

                alarmRepository.addAlarm(alarm)
            }

            is AlarmIntent.LoadSystemRingtones -> loadRingtones()
            is AlarmIntent.PlayPreview -> audioPlayer.playAlarm(
                intent.uri,
                loop = false,
                alarm = false,
            )

            is AlarmIntent.StopPreview -> audioPlayer.stop()
        }
    }

    private fun observeAlarms() {
        _uiState.update { it.copy(isLoading = true) }
        alarmRepository.allAlarms
            .onEach { alarmList ->
                _uiState.update { it.copy(alarms = alarmList, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadRingtones() {
        viewModelScope.launch {
            val ringtones = ringtoneRepository.getSystemAlarms()
            _uiState.update { it.copy(ringtones = ringtones) }
        }
    }

    override fun onCleared() {
        audioPlayer.stop()
    }
}
