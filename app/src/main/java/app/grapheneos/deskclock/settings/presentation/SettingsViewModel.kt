package app.grapheneos.deskclock.settings.presentation

import android.app.Application
import android.media.RingtoneManager
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.alarm.presentation.RingtoneItem
import app.grapheneos.deskclock.core.audio.AudioPlayer
import app.grapheneos.deskclock.settings.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val application: Application,
    private val audioPlayer: AudioPlayer,
    private val repository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        handleIntent(SettingsIntent.LoadSettings)
        handleIntent(SettingsIntent.LoadSystemRingtones)
    }

    fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.LoadSettings -> observeSettings()
            is SettingsIntent.ResetDefaults -> update { repository.resetToDefaults() }
            is SettingsIntent.LoadSystemRingtones -> loadRingtones()
            is SettingsIntent.PlayPreview -> audioPlayer.playAlarm(
                intent.uri,
                loop = false,
                alarm = false
            )
            is SettingsIntent.StopPreview -> audioPlayer.stop()

            is SettingsIntent.UpdateTheme -> update { repository.setTheme(intent.theme) }
            is SettingsIntent.SetDynamicColors -> update { repository.setDynamicColors(intent.enabled) }

            is SettingsIntent.SetSnoozeTime -> update { repository.setSnoozeTime(intent.minutes) }
            is SettingsIntent.SetDefaultRingtone -> update {
                val ringtone = RingtoneManager.getRingtone(
                    application.applicationContext,
                    intent.uri.toUri()
                )
                repository.setDefaultRingtone(
                    RingtoneItem(
                        ringtone.getTitle(application.applicationContext),
                        intent.uri
                    )
                )
            }

            is SettingsIntent.SetCustomRingtoneVolumeEnabled -> update {
                repository.setCustomRingtoneVolumeEnabled(intent.enabled)
            }

            is SettingsIntent.SetCustomRingtoneVolume -> update {
                repository.setRingtoneVolume(intent.volume)
            }

            is SettingsIntent.SetDefaultVibration -> update { repository.setDefaultVibration(intent.enabled) }
        }
    }

    private fun observeSettings() {
        repository.settings
            .onEach { updatedSettings ->
                _state.update { it.copy(settings = updatedSettings) }
            }
            .launchIn(viewModelScope)
    }

    private fun update(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
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
}
