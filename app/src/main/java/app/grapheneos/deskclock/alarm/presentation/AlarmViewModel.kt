package app.grapheneos.deskclock.alarm.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.alarm.data.AlarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlarmViewModel(
    private val repository: AlarmRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AlarmState())
    val state: StateFlow<AlarmState> = _state.asStateFlow()

    init {
        handleIntent(AlarmAction.LoadAlarms)
    }

    fun handleIntent(intent: AlarmAction) {
        viewModelScope.launch {
            when (intent) {
                AlarmAction.LoadAlarms -> observeAlarms()
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
}
