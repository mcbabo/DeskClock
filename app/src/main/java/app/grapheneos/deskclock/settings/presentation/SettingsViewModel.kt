package app.grapheneos.deskclock.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.core.audio.AudioPlayer
import app.grapheneos.deskclock.core.ringtone.RingtoneRepository
import app.grapheneos.deskclock.settings.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val audioPlayer: AudioPlayer,
    private val settingsRepository: SettingsRepository,
    private val ringtoneRepository: RingtoneRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var settingsJob: kotlinx.coroutines.Job? = null

    init {
        handleIntent(SettingsIntent.LoadSettings)
        handleIntent(SettingsIntent.LoadSystemRingtones)
        handleIntent(SettingsIntent.LoadRawRingtones)
    }

    fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.LoadSettings -> observeSettings()
            is SettingsIntent.ResetDefaults -> update { settingsRepository.resetToDefaults() }
            is SettingsIntent.LoadSystemRingtones -> loadRingtones()
            is SettingsIntent.LoadRawRingtones -> loadRawRingtones()
            is SettingsIntent.PlayPreview -> {
                audioPlayer.playAlarm(intent.uri, loop = false, alarm = false)
            }

            is SettingsIntent.StopPreview -> audioPlayer.stop()
            is SettingsIntent.UpdateTheme -> update { settingsRepository.setTheme(intent.theme) }
            is SettingsIntent.SetDynamicColors -> update {
                settingsRepository.setDynamicColors(
                    intent.enabled
                )
            }

            is SettingsIntent.SetSnoozeTime -> update { settingsRepository.setSnoozeTime(intent.minutes) }
            is SettingsIntent.SetDefaultRingtone -> viewModelScope.launch {
                val ringtoneItem = ringtoneRepository.getRingtoneItem(intent.uri)
                settingsRepository.setDefaultRingtone(ringtoneItem)
            }

            is SettingsIntent.SetDirectBootRingtone -> viewModelScope.launch {
                val ringtoneItem = if (intent.uri.startsWith("android.resource")) {
                    _uiState.value.rawRingtones.find { it.uri == intent.uri }
                } else {
                    ringtoneRepository.getRingtoneItem(intent.uri)
                }
                ringtoneItem?.let { settingsRepository.setDirectBootRingtone(it) }
            }

            is SettingsIntent.SetCustomRingtoneVolumeEnabled -> update {
                settingsRepository.setCustomRingtoneVolumeEnabled(intent.enabled)
            }

            is SettingsIntent.SetCustomRingtoneVolume -> update {
                settingsRepository.setRingtoneVolume(intent.volume)
            }

            is SettingsIntent.SetDefaultVibration -> update {
                settingsRepository.setDefaultVibration(
                    intent.enabled
                )
            }

            is SettingsIntent.SetAlarmPopUpStyle -> update {
                settingsRepository.setAlarmPopUpStyle(intent.style)
            }

            is SettingsIntent.SetTimerPopUpStyle -> update {
                settingsRepository.setTimerPopUpStyle(intent.style)
            }

            is SettingsIntent.SetGraduallyIncreaseVolume -> update {
                settingsRepository.setGraduallyIncreaseVolume(intent.enabled)
            }

            is SettingsIntent.SetGraduallyIncreaseVolumeDuration -> update {
                settingsRepository.setGraduallyIncreaseVolumeDuration(intent.duration)
            }
        }
    }

    private fun observeSettings() {
        if (settingsJob != null) return
        settingsJob = settingsRepository.settings
            .onEach { updatedSettings ->
                _uiState.update { it.copy(settings = updatedSettings.toUiModel()) }
            }
            .launchIn(viewModelScope)
    }

    private fun update(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }

    private fun loadRingtones() {
        viewModelScope.launch {
            val ringtones = ringtoneRepository.getSystemAlarms()
            _uiState.update { it.copy(ringtones = ringtones) }
        }
    }

    private fun loadRawRingtones() {
        viewModelScope.launch {
            val ringtones = ringtoneRepository.getRawRingtones()
            _uiState.update { it.copy(rawRingtones = ringtones) }
        }
    }
}
