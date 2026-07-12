package app.grapheneos.deskclock.alarm.presentation

import android.app.Application
import android.media.RingtoneManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.alarm.data.AlarmRepository
import app.grapheneos.deskclock.alarm.util.AlarmSoundPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlarmViewModel(
    private val repository: AlarmRepository,
    private val application: Application
) : ViewModel() {

    private val _state = MutableStateFlow(AlarmState())
    val state: StateFlow<AlarmState> = _state.asStateFlow()

    val soundPlayer = AlarmSoundPlayer(application)

    init {
        handleIntent(AlarmAction.LoadAlarms)
        handleIntent(AlarmAction.LoadSystemRingtones)
    }

    fun handleIntent(intent: AlarmAction) {
        viewModelScope.launch {
            when (intent) {
                is AlarmAction.LoadAlarms -> observeAlarms()
                is AlarmAction.ToggleAlarm -> repository.toggleAlarm(intent.alarm)
                is AlarmAction.UpdateAlarm -> repository.updateAlarm(intent.alarm)
                is AlarmAction.DeleteAlarm -> repository.deleteAlarm(intent.alarm)
                is AlarmAction.AddAlarm -> {
                    repository.addAlarm(
                        hour = intent.hour,
                        minute = intent.minute,
                        daysOfWeek = intent.daysOfWeek,
                        deleteAfterUse = intent.deleteAfterUse,
                        label = intent.label
                    )
                }

                is AlarmAction.LoadSystemRingtones -> loadRingtones()
                is AlarmAction.PlayPreview -> soundPlayer.playPreview(intent.uri)
                is AlarmAction.StopPreview -> soundPlayer.stop()
            }
        }
    }

    private fun observeAlarms() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.allAlarms.collect { alarmList ->
                _state.update { it.copy(alarms = alarmList, isLoading = false) }
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
        _state.update { it.copy(ringtones = list) }
    }

    override fun onCleared() {
        soundPlayer.stop()
        super.onCleared()
    }
}
