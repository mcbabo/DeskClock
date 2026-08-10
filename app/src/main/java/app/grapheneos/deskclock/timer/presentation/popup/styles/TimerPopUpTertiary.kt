package app.grapheneos.deskclock.timer.presentation.popup.styles

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.core.presentation.SwipeAction
import app.grapheneos.deskclock.core.presentation.SwipeSlider
import app.grapheneos.deskclock.core.theme.DeskClockTheme

@Composable
fun TimerPopUpTertiary(
    remainingTime: Long,
    onStop: () -> Unit,
    onAddMinute: () -> Unit
) {
    TimerPopUpBase(
        remainingTime = remainingTime
    ) {
        SwipeSlider(
            centerLabel = stringResource(R.string.swipe_to_dismiss),
            modifier = Modifier.fillMaxWidth(),
            leftAction = SwipeAction(
                icon = Icons.Default.Add,
                label = stringResource(R.string.add_minute),
                color = { MaterialTheme.colorScheme.tertiary },
                onTrigger = onAddMinute
            ),
            rightAction = SwipeAction(
                icon = Icons.Default.Stop,
                label = stringResource(R.string.stop),
                color = { MaterialTheme.colorScheme.error },
                onTrigger = onStop
            )
        )
    }
}

@Preview
@Composable
fun TimerPopUpTertiaryPreview() {
    DeskClockTheme {
        TimerPopUpTertiary(remainingTime = 0L, onStop = {}, onAddMinute = {})
    }
}
