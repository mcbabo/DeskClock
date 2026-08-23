package app.grapheneos.deskclock.alarm.presentation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.alarm.data.AlarmEntity
import app.grapheneos.deskclock.alarm.data.AlarmRepository
import app.grapheneos.deskclock.alarm.util.AlarmTimeCalculator
import app.grapheneos.deskclock.core.audio.AudioPlayer
import app.grapheneos.deskclock.core.ringtone.RingtoneItem
import app.grapheneos.deskclock.core.ringtone.RingtoneRepository
import app.grapheneos.deskclock.settings.data.SettingsRepository
import app.grapheneos.deskclock.settings.presentation.toUiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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

    private data class InternalState(
        val ringtones: List<RingtoneItem> = emptyList(),
        val editingAlarmId: Long? = null,
        val isTimePickerVisible: Boolean = false
    )

    private val internalState = MutableStateFlow(InternalState())

    private val settings = settingsRepository.settings
        .map { it.toUiModel() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val uiState: StateFlow<AlarmUiState> = combine(
        alarmRepository.allAlarms,
        internalState,
        flow {
            while (true) {
                emit(Unit)
                delay(1.minutes)
            }
        }
    ) { alarms, internal, _ ->
        val uiModels = alarms.map { it.toUiModel(application) }
        AlarmUiState(
            alarms = uiModels,
            ringtones = internal.ringtones,
            isLoading = false,
            nextAlarmRemainingTime = calculateNextAlarmString(uiModels),
            editingAlarmId = internal.editingAlarmId,
            isTimePickerVisible = internal.isTimePickerVisible
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AlarmUiState(isLoading = true)
    )

    init {
        refreshRingtones()
    }

    private fun refreshRingtones() = viewModelScope.launch {
        val ringtones = ringtoneRepository.getRingtones()
        internalState.update { it.copy(ringtones = ringtones) }
    }

    fun handleIntent(intent: AlarmIntent) {
        viewModelScope.launch {
            when (intent) {
                is AlarmIntent.ToggleAlarm -> alarmRepository.toggleAlarm(intent.alarm.toEntity())
                is AlarmIntent.UpdateAlarm -> alarmRepository.updateAlarm(intent.alarm.toEntity())
                is AlarmIntent.DeleteAlarm -> alarmRepository.deleteAlarm(intent.alarm.toEntity())
                is AlarmIntent.RestoreAlarm -> alarmRepository.addAlarm(intent.alarm.toEntity())
                is AlarmIntent.AddAlarm -> {
                    val currentSettings = settings.value ?: return@launch
                    alarmRepository.addAlarm(
                        AlarmEntity(
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
                    )
                }

                is AlarmIntent.ImportRingtone -> {
                    ringtoneRepository.addCustomRingtone(intent.uri)?.let { refreshRingtones() }
                }

                is AlarmIntent.DeleteCustomRingtone -> {
                    ringtoneRepository.deleteCustomRingtone(intent.ringtone)
                    settingsRepository.onRingtoneDeleted(intent.ringtone.uri)
                    val currentSettings = settingsRepository.settings.first()
                    alarmRepository.updateRingtoneUri(
                        oldUri = intent.ringtone.uri,
                        newUri = currentSettings.defaultRingtone.uri
                    )
                    refreshRingtones()
                }

                is AlarmIntent.PlayPreview -> audioPlayer.playAlarm(
                    intent.uri,
                    loop = false,
                    alarm = false
                )

                is AlarmIntent.StopPreview -> audioPlayer.stop()
                is AlarmIntent.SetEditingAlarm -> internalState.update {
                    it.copy(editingAlarmId = intent.id)
                }

                is AlarmIntent.SetTimePickerVisible -> internalState.update {
                    it.copy(isTimePickerVisible = intent.visible)
                }
            }
        }
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
