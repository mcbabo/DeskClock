package app.grapheneos.deskclock.alarm.presentation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.alarm.data.AlarmEntity
import app.grapheneos.deskclock.alarm.data.AlarmRepository
import app.grapheneos.deskclock.alarm.util.AlarmTimeCalculator
import app.grapheneos.deskclock.core.audio.AudioPlayer
import app.grapheneos.deskclock.core.ringtone.RingtoneRepository
import app.grapheneos.deskclock.settings.data.SettingsRepository
import app.grapheneos.deskclock.settings.presentation.toUiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import kotlin.time.Duration.Companion.minutes

class AlarmViewModel(
    private val application: Application,
    private val alarmRepository: AlarmRepository,
    private val settingsRepository: SettingsRepository,
    private val ringtoneRepository: RingtoneRepository,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    private val settings = settingsRepository.settings
        .map { it.toUiModel() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    init {
        observeAlarms()
        observeNextAlarmTime()
        refreshRingtones()
    }

    private fun refreshRingtones() {
        viewModelScope.launch {
            val allRingtones = ringtoneRepository.getRingtones()
            _uiState.update { it.copy(ringtones = allRingtones) }
        }
    }

    fun handleIntent(intent: AlarmIntent) {
        when (intent) {
            is AlarmIntent.ToggleAlarm -> viewModelScope.launch {
                alarmRepository.toggleAlarm(intent.alarm.toEntity())
            }

            is AlarmIntent.UpdateAlarm -> viewModelScope.launch {
                alarmRepository.updateAlarm(intent.alarm.toEntity())
            }

            is AlarmIntent.DeleteAlarm -> viewModelScope.launch {
                alarmRepository.deleteAlarm(intent.alarm.toEntity())
            }

            is AlarmIntent.AddAlarm -> viewModelScope.launch {
                val currentSettings = settings.value ?: return@launch
                val alarm = AlarmEntity(
                    hour = intent.hour,
                    minute = intent.minute,
                    daysOfWeek = intent.daysOfWeek,
                    isEnabled = true,
                    deleteAfterUse = intent.deleteAfterUse,
                    label = intent.label,
                    ringtoneUri = currentSettings.defaultRingtone.uri,
                    vibrate = currentSettings.vibrate,
                    snoozeDurationMinutes = currentSettings.snoozeDurationMinutes
                )
                alarmRepository.addAlarm(alarm)
            }

            is AlarmIntent.ImportRingtone -> viewModelScope.launch {
                ringtoneRepository.addCustomRingtone(intent.uri)?.let { _ ->
                    refreshRingtones()
                }
            }

            is AlarmIntent.DeleteCustomRingtone -> viewModelScope.launch {
                ringtoneRepository.deleteCustomRingtone(intent.ringtone)
                settingsRepository.onRingtoneDeleted(intent.ringtone.uri)
                val currentSettings = settingsRepository.settings.first()
                alarmRepository.updateRingtoneUri(
                    oldUri = intent.ringtone.uri,
                    newUri = currentSettings.defaultRingtone.uri
                )
                refreshRingtones()
            }

            is AlarmIntent.PlayPreview -> {
                audioPlayer.playAlarm(intent.uri, loop = false, alarm = false)
            }

            is AlarmIntent.StopPreview -> audioPlayer.stop()

            else -> {}
        }
    }

    private fun observeAlarms() {
        _uiState.update { it.copy(isLoading = true) }
        alarmRepository.allAlarms
            .onEach { alarmList ->
                val uiModels = alarmList.map { it.toUiModel(application) }
                _uiState.update { it.copy(alarms = uiModels, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeNextAlarmTime() {
        flow {
            while (true) {
                emit(Unit)
                delay(1.minutes)
            }
        }.combine(alarmRepository.allAlarms) { _, alarms ->
            calculateNextAlarmString(alarms.map { it.toUiModel(application) })
        }.onEach { nextAlarmString ->
            _uiState.update { it.copy(nextAlarmRemainingTime = nextAlarmString) }
        }.launchIn(viewModelScope)
    }

    private fun calculateNextAlarmString(alarms: List<AlarmUiModel>): String? {
        val activeAlarms = alarms.filter { it.isEnabled }
        val nextTriggerTime = activeAlarms.minOfOrNull {
            AlarmTimeCalculator.calculateNextTriggerTime(it.hour, it.minute, it.daysOfWeek)
        } ?: return null

        val duration = Duration.between(Instant.now(), Instant.ofEpochMilli(nextTriggerTime))

        return if (duration.isNegative || duration.isZero) {
            null
        } else {
            mutableListOf<String>().apply {
                val days = duration.toDays()
                if (days > 0) add("${days}d")
                if (days > 0 || duration.toHoursPart() > 0) add("${duration.toHoursPart()}h")
                add("${duration.toMinutesPart()}m")
            }.joinToString(" ")
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }
}
