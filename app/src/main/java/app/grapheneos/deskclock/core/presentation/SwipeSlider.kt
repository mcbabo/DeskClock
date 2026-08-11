package app.grapheneos.deskclock.core.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.core.util.Constants
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SwipeSlider(
    centerLabel: String,
    modifier: Modifier = Modifier,
    leftAction: SwipeAction? = null,
    rightAction: SwipeAction? = null,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val haptic = LocalHapticFeedback.current
    var hasTriggeredHaptic by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(42.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .semantics {
                role = Role.Button
                contentDescription = centerLabel
            }
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val thumbSizePx = with(density) { 76.dp.toPx() }
        val marginPx = with(density) { 8.dp.toPx() }
        val maxTravel = (widthPx - thumbSizePx) / 2f - marginPx
        val centerOffset = (widthPx - thumbSizePx) / 2f

        val isRightReached = rightAction != null && offsetX.value > maxTravel * 0.6f
        val isLeftReached = leftAction != null && offsetX.value < -maxTravel * 0.6f
        val isAnyReached = isRightReached || isLeftReached

        LaunchedEffect(isAnyReached) {
            if (isAnyReached && !hasTriggeredHaptic) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                hasTriggeredHaptic = true
            } else if (!isAnyReached) {
                hasTriggeredHaptic = false
            }
        }

        SwipeSliderContent(
            centerLabel = centerLabel,
            leftAction = leftAction,
            rightAction = rightAction,
            offsetX = { offsetX.value },
            maxTravel = maxTravel,
            centerOffset = centerOffset,
            isLeftReached = { leftAction != null && offsetX.value < -maxTravel * 0.6f },
            isRightReached = { rightAction != null && offsetX.value > maxTravel * 0.6f },
            onOffsetChange = { delta ->
                val min = if (leftAction != null) -maxTravel else 0f
                val max = if (rightAction != null) maxTravel else 0f
                scope.launch { offsetX.snapTo((offsetX.value + delta).coerceIn(min, max)) }
            },
            onDragStopped = {
                scope.launch {
                    val reachedRight = rightAction != null && offsetX.value > maxTravel * 0.6f
                    val reachedLeft = leftAction != null && offsetX.value < -maxTravel * 0.6f
                    when {
                        reachedRight -> {
                            offsetX.animateTo(maxTravel, tween(150))
                            rightAction.onTrigger()
                            offsetX.animateTo(0f, tween(300))
                        }

                        reachedLeft -> {
                            offsetX.animateTo(-maxTravel, tween(150))
                            leftAction.onTrigger()
                            offsetX.animateTo(0f, tween(300))
                        }

                        else -> offsetX.animateTo(0f, tween(300))
                    }
                }
            }
        )
    }
}

@Composable
private fun SwipeSliderContent(
    centerLabel: String,
    leftAction: SwipeAction?,
    rightAction: SwipeAction?,
    offsetX: () -> Float,
    maxTravel: Float,
    centerOffset: Float,
    isLeftReached: () -> Boolean,
    isRightReached: () -> Boolean,
    onOffsetChange: (Float) -> Unit,
    onDragStopped: () -> Unit
) {
    val reachedRight = isRightReached()
    val reachedLeft = isLeftReached()

    val targetColor = when {
        reachedRight -> rightAction?.color?.invoke() ?: MaterialTheme.colorScheme.primary
        reachedLeft -> leftAction?.color?.invoke() ?: MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.primary
    }
    val thumbColor by animateColorAsState(
        targetValue = targetColor,
        label = Constants.COMPOSE_LABEL_THUMB_COLOR
    )

    SwipeBackgroundIcons(leftAction, rightAction, offsetX)

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = when {
                reachedRight -> rightAction?.label ?: centerLabel
                reachedLeft -> leftAction?.label ?: centerLabel
                else -> centerLabel
            },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.graphicsLayer {
                alpha = (1f - abs(offsetX()) / (maxTravel * 0.5f)).coerceIn(0f, 1f)
            }
        )
    }

    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .offset { IntOffset((centerOffset + offsetX()).roundToInt(), 0) }
            .size(76.dp)
            .clip(CircleShape)
            .background(thumbColor)
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState(onOffsetChange),
                onDragStopped = { onDragStopped() }
            ),
        contentAlignment = Alignment.Center
    ) {
        val icon = when {
            reachedRight -> rightAction?.icon
            reachedLeft -> leftAction?.icon
            else -> rightAction?.icon ?: leftAction?.icon
        } ?: Icons.Default.Close

        val tint = when {
            reachedRight -> if (targetColor == MaterialTheme.colorScheme.error) {
                MaterialTheme.colorScheme.onError
            } else {
                MaterialTheme.colorScheme.onTertiary
            }

            reachedLeft -> MaterialTheme.colorScheme.onSecondary
            else -> MaterialTheme.colorScheme.onPrimary
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun SwipeBackgroundIcons(
    leftAction: SwipeAction?,
    rightAction: SwipeAction?,
    offsetX: () -> Float
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leftAction != null) {
            Icon(
                imageVector = leftAction.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.graphicsLayer { alpha = if (offsetX() < 0) 1f else 0.3f }
            )
        } else {
            Box(Modifier.size(24.dp))
        }

        if (rightAction != null) {
            Icon(
                imageVector = rightAction.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.graphicsLayer { alpha = if (offsetX() > 0) 1f else 0.3f }
            )
        } else {
            Box(Modifier.size(24.dp))
        }
    }
}
