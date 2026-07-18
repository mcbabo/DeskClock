package app.grapheneos.deskclock.core.presentation

import androidx.compose.ui.graphics.vector.ImageVector

data class PermissionUiState(
    val title: String,
    val description: String,
    val isGranted: Boolean,
    val icon: ImageVector,
    val launchAction: () -> Unit
)
