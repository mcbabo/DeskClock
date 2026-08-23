package app.grapheneos.deskclock.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grapheneos.deskclock.settings.data.SettingsRepository
import app.grapheneos.deskclock.settings.presentation.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class MainActivityViewModel(
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val intentAction = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MainUiState> = combine(
        settingsRepository.settings.map { it.toUiModel() },
        intentAction
    ) { settings, action ->
        MainUiState(
            settings = settings,
            initialAction = action
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState()
    )

    fun onNewIntentAction(action: String?) {
        intentAction.update { action }
    }
}
