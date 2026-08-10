package app.grapheneos.deskclock.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class SwipeAction(
    val icon: ImageVector,
    val label: String,
    val color: @Composable () -> Color,
    val onTrigger: () -> Unit
)
