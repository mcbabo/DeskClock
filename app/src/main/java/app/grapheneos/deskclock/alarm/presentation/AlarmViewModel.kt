package app.grapheneos.deskclock.alarm.presentation

import android.app.Application
import android.media.RingtoneManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.alarm.data.AlarmRepository
import app.grapheneos.deskclock.core.audio.AudioPlayer
import app.grapheneos.deskclock.settings.data.AppSettings
import app.grapheneos.deskclock.settings.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlarmViewModel(
    private val repository: AlarmRepository,
    private val settingsRepository: SettingsRepository,
    private val application: Application,
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
        handleAction(AlarmAction.LoadAlarms)
        handleAction(AlarmAction.LoadSystemRingtones)
    }

    fun handleAction(intent: AlarmAction) {
        viewModelScope.launch {
            when (intent) {
                is AlarmAction.LoadAlarms -> observeAlarms()
                is AlarmAction.ToggleAlarm -> repository.toggleAlarm(intent.alarm)
                is AlarmAction.UpdateAlarm -> repository.updateAlarm(intent.alarm)
                is AlarmAction.DeleteAlarm -> repository.deleteAlarm(intent.alarm)
                is AlarmAction.AddAlarm -> {
                    val settings = settings.value

                    repository.addAlarm(
                        hour = intent.hour,
                        minute = intent.minute,
                        daysOfWeek = intent.daysOfWeek,
                        deleteAfterUse = intent.deleteAfterUse,
                        label = intent.label,
                        ringtoneUri = settings.defaultRingtone.uri,
                        vibrate = settings.vibrate,
                        snoozeDurationMinutes = settings.snoozeDurationMinutes
                    )
                }

                is AlarmAction.LoadSystemRingtones -> loadRingtones()
                is AlarmAction.PlayPreview -> audioPlayer.playAlarm(
                    intent.uri,
                    loop = false,
                    alarm = false
                )

                is AlarmAction.StopPreview -> audioPlayer.stop()
            }
        }
    }

    private fun observeAlarms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.allAlarms.collect { alarmList ->
                _uiState.update { it.copy(alarms = alarmList, isLoading = false) }
            }
        }
    }

    private fun loadRingtones() {
        val manager = RingtoneManager(application).apply {
            setType(RingtoneManager.TYPE_ALARM)
        }
        val cursor = manager.cursor
        val list = mutableListOf<RingtoneItem>()
        while (cursor.moveToNext()) {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val uri = manager.getRingtoneUri(cursor.position).toString()
            list.add(RingtoneItem(title, uri))
        }
        _uiState.update { it.copy(ringtones = list) }
    }

    override fun onCleared() {
        audioPlayer.stop()
        super.onCleared()
    }
}
