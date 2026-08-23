package app.grapheneos.deskclock.core.presentation

import androidx.compose.runtime.Immutable
import app.grapheneos.deskclock.settings.presentation.AppSettingsUiModel

@Immutable
data class MainUiState(
    val settings: AppSettingsUiModel? = null,
    val initialAction: String? = null
)
