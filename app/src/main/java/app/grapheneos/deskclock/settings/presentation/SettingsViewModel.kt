package app.grapheneos.deskclock.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.alarm.data.AlarmRepository
import app.grapheneos.deskclock.core.audio.AudioPlayer
import app.grapheneos.deskclock.core.ringtone.RingtoneItem
import app.grapheneos.deskclock.core.ringtone.RingtoneRepository
import app.grapheneos.deskclock.core.util.UriScheme
import app.grapheneos.deskclock.core.util.uriScheme
import app.grapheneos.deskclock.settings.data.AppSettings
import app.grapheneos.deskclock.settings.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val audioPlayer: AudioPlayer,
    private val settingsRepository: SettingsRepository,
    private val alarmRepository: AlarmRepository,
    private val ringtoneRepository: RingtoneRepository,
) : ViewModel() {

    private data class InternalState(
        val ringtones: List<RingtoneItem> = emptyList(),
        val internalRingtones: List<RingtoneItem> = emptyList(),
        val visibleDialog: SettingsDialog = SettingsDialog.NONE
    )

    private val internalState = MutableStateFlow(InternalState())

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settings,
        internalState
    ) { settings, internal ->
        SettingsUiState(
            settings = settings.toUiModel(),
            ringtones = internal.ringtones,
            internalRingtones = internal.internalRingtones,
            visibleDialog = internal.visibleDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    init {
        refreshRingtones()
    }

    private fun refreshRingtones() {
        viewModelScope.launch {
            val allRingtones = ringtoneRepository.getRingtones()
            val internalRingtones = ringtoneRepository.getInternalRingtones()
            internalState.update {
                it.copy(
                    ringtones = allRingtones,
                    internalRingtones = internalRingtones
                )
            }
        }
    }

    fun handleIntent(intent: SettingsIntent) {
        viewModelScope.launch {
            when (intent) {
                is SettingsIntent.LoadSettings -> {}
                is SettingsIntent.RefreshRingtones -> refreshRingtones()
                is SettingsIntent.ResetDefaults -> settingsRepository.update { AppSettings() }
                is SettingsIntent.LoadSystemRingtones -> {
                    val ringtones = ringtoneRepository.getRingtones()
                    internalState.update { it.copy(ringtones = ringtones) }
                }

                is SettingsIntent.LoadInternalRingtones -> {
                    val internalRingtones = ringtoneRepository.getInternalRingtones()
                    internalState.update { it.copy(internalRingtones = internalRingtones) }
                }

                is SettingsIntent.ImportRingtone -> {
                    ringtoneRepository.addCustomRingtone(intent.uri)?.let { refreshRingtones() }
                }

                is SettingsIntent.DeleteCustomRingtone -> {
                    ringtoneRepository.deleteCustomRingtone(intent.ringtone)
                    settingsRepository.onRingtoneDeleted(intent.ringtone.uri)
                    val currentSettings = settingsRepository.settings.first()
                    alarmRepository.updateRingtoneUri(
                        oldUri = intent.ringtone.uri,
                        newUri = currentSettings.defaultRingtone.uri
                    )
                    refreshRingtones()
                }

                is SettingsIntent.PlayPreview -> audioPlayer.playAlarm(
                    intent.uri,
                    loop = false,
                    alarm = false
                )

                is SettingsIntent.StopPreview -> audioPlayer.stop()
                is SettingsIntent.UpdateTheme -> settingsRepository.update { it.copy(themeMode = intent.theme) }
                is SettingsIntent.SetDynamicColors -> settingsRepository.update {
                    it.copy(
                        dynamicColors = intent.enabled
                    )
                }

                is SettingsIntent.SetSnoozeTime -> settingsRepository.update {
                    it.copy(
                        snoozeDurationMinutes = intent.minutes
                    )
                }

                is SettingsIntent.SetDefaultRingtone -> {
                    val ringtoneItem = ringtoneRepository.getRingtoneItem(intent.uri)
                    settingsRepository.update { it.copy(defaultRingtone = ringtoneItem) }
                }

                is SettingsIntent.SetDirectBootRingtone -> {
                    val ringtoneItem = if (intent.uri.uriScheme == UriScheme.RESOURCE) {
                        uiState.value.internalRingtones.find { it.uri == intent.uri }
                    } else {
                        ringtoneRepository.getRingtoneItem(intent.uri)
                    }
                    ringtoneItem?.let { item ->
                        settingsRepository.update { it.copy(directBootRingtone = item) }
                    }
                }

                is SettingsIntent.SetCustomRingtoneVolumeEnabled -> settingsRepository.update {
                    it.copy(useCustomRingtoneVolume = intent.enabled)
                }

                is SettingsIntent.SetCustomRingtoneVolume -> settingsRepository.update {
                    it.copy(ringtoneVolume = intent.volume)
                }

                is SettingsIntent.SetDefaultVibration -> settingsRepository.update { it.copy(vibrate = intent.enabled) }
                is SettingsIntent.SetAlarmPopUpStyle -> settingsRepository.update {
                    it.copy(
                        alarmPopUpStyle = intent.style
                    )
                }

                is SettingsIntent.SetTimerPopUpStyle -> settingsRepository.update {
                    it.copy(
                        timerPopUpStyle = intent.style
                    )
                }

                is SettingsIntent.SetGraduallyIncreaseVolume -> settingsRepository.update {
                    it.copy(graduallyIncreaseVolume = intent.enabled)
                }

                is SettingsIntent.SetGraduallyIncreaseVolumeDuration -> settingsRepository.update {
                    it.copy(graduallyIncreaseVolumeDuration = intent.duration)
                }

                is SettingsIntent.SetStopwatchShowMilliseconds -> settingsRepository.update {
                    it.copy(stopwatchShowMilliseconds = intent.enabled)
                }

                is SettingsIntent.SetVisibleDialog -> internalState.update { it.copy(visibleDialog = intent.dialog) }
            }
        }
    }
}
