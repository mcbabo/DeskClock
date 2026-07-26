package app.grapheneos.deskclock.stopwatch.presentation.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StopwatchControls(
    isRunning: Boolean,
    canReset: Boolean,
    onStartPause: () -> Unit,
    onLapOrReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledIconButton(
            onClick = {
                view.performHapticFeedback(
                    if (!isRunning) {
                        HapticFeedbackConstants.SEGMENT_TICK
                    } else {
                        HapticFeedbackConstants.VIRTUAL_KEY
                    }
                )
                onLapOrReset()
            },
            enabled = isRunning || canReset,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            modifier = Modifier.size(64.dp)
        ) {
            AnimatedContent(
                targetState = isRunning,
                transitionSpec = { expressiveIconTransition() },
                label = "lap_reset_icon"
            ) { running ->
                Icon(
                    imageVector = if (running) Icons.Filled.Flag else Icons.Filled.RestartAlt,
                    contentDescription = stringResource(
                        if (running) R.string.lap else R.string.reset
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        FilledIconToggleButton(
            checked = isRunning,
            onCheckedChange = { _ ->
                view.performHapticFeedback(
                    if (!isRunning) {
                        HapticFeedbackConstants.CONFIRM
                    } else {
                        HapticFeedbackConstants.REJECT
                    }
                )
                onStartPause()
            },
            shapes = IconButtonDefaults.toggleableShapes(
                shape = CircleShape,
                checkedShape = MaterialShapes.Cookie9Sided.toShape()
            ),
            colors = IconButtonDefaults.filledIconToggleButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                checkedContainerColor = MaterialTheme.colorScheme.errorContainer,
                checkedContentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            modifier = Modifier.size(96.dp)
        ) {
            AnimatedContent(
                targetState = isRunning,
                transitionSpec = { expressiveIconTransition() },
                label = "start_pause_icon"
            ) { running ->
                Icon(
                    imageVector = if (running) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = stringResource(
                        if (running) R.string.pause else R.string.start
                    ),
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

private fun expressiveIconTransition(): ContentTransform =
    (scaleIn(initialScale = 0.6f) + fadeIn()) togetherWith (scaleOut(targetScale = 0.6f) + fadeOut())
