package app.grapheneos.deskclock.core.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FloatingActionButton(
    modifier: Modifier = Modifier,
    text: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    isExpanded: Boolean,
    isVisible: Boolean,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        ExtendedFloatingActionButton(
            text = text,
            icon = icon,
            onClick = onClick,
            modifier = modifier,
            expanded = isExpanded,
        )
    }
}
