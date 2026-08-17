package app.grapheneos.deskclock.settings.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.alarm.data.AlarmRepository
import app.grapheneos.deskclock.core.audio.AudioPlayer
import app.grapheneos.deskclock.core.ringtone.RingtoneRepository
import app.grapheneos.deskclock.settings.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val audioPlayer: AudioPlayer,
    private val settingsRepository: SettingsRepository,
    private val alarmRepository: AlarmRepository,
    private val ringtoneRepository: RingtoneRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var settingsJob: kotlinx.coroutines.Job? = null

    init {
        handleIntent(SettingsIntent.LoadSettings)
        handleIntent(SettingsIntent.RefreshRingtones)
    }

    fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.LoadSettings -> observeSettings()
            is SettingsIntent.RefreshRingtones -> {
                loadAllRingtones()
                loadInternalRingtones()
            }

            is SettingsIntent.ResetDefaults -> update { settingsRepository.resetToDefaults() }
            is SettingsIntent.LoadSystemRingtones -> loadAllRingtones()
            is SettingsIntent.LoadInternalRingtones -> loadInternalRingtones()
            is SettingsIntent.ImportRingtone -> importCustomRingtone(intent.uri)
            is SettingsIntent.DeleteCustomRingtone -> viewModelScope.launch {
                ringtoneRepository.deleteCustomRingtone(intent.ringtone)
                settingsRepository.onRingtoneDeleted(intent.ringtone.uri)
                val currentSettings = settingsRepository.settings.first()
                alarmRepository.updateRingtoneUri(
                    oldUri = intent.ringtone.uri,
                    newUri = currentSettings.defaultRingtone.uri
                )
                handleIntent(SettingsIntent.RefreshRingtones)
            }

            is SettingsIntent.PlayPreview -> {
                audioPlayer.playAlarm(intent.uri, loop = false, alarm = false)
            }

            is SettingsIntent.StopPreview -> audioPlayer.stop()
            is SettingsIntent.UpdateTheme -> update { settingsRepository.setTheme(intent.theme) }
            is SettingsIntent.SetDynamicColors -> update {
                settingsRepository.setDynamicColors(intent.enabled)
            }

            is SettingsIntent.SetSnoozeTime -> update { settingsRepository.setSnoozeTime(intent.minutes) }
            is SettingsIntent.SetDefaultRingtone -> viewModelScope.launch {
                val ringtoneItem = ringtoneRepository.getRingtoneItem(intent.uri)
                settingsRepository.setDefaultRingtone(ringtoneItem)
            }

            is SettingsIntent.SetDirectBootRingtone -> viewModelScope.launch {
                val ringtoneItem = if (intent.uri.scheme == "android.resource") {
                    _uiState.value.internalRingtones.find { it.uri == intent.uri }
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
                settingsRepository.setDefaultVibration(intent.enabled)
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

    private fun loadAllRingtones() {
        viewModelScope.launch {
            val allRingtones = ringtoneRepository.getRingtones()
            _uiState.update { it.copy(ringtones = allRingtones) }
        }
    }

    private fun importCustomRingtone(uri: Uri) {
        viewModelScope.launch {
            ringtoneRepository.addCustomRingtone(uri)?.let { _ ->
                loadAllRingtones()
            }
        }
    }

    private fun loadInternalRingtones() {
        viewModelScope.launch {
            val ringtones = ringtoneRepository.getInternalRingtones()
            _uiState.update { it.copy(internalRingtones = ringtones) }
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
}
