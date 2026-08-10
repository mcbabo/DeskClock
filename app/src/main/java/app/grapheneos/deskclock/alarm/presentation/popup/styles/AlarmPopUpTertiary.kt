package app.grapheneos.deskclock.alarm.presentation.popup.styles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.alarm.presentation.AlarmUiModel
import app.grapheneos.deskclock.core.presentation.SwipeAction
import app.grapheneos.deskclock.core.presentation.SwipeSlider
import app.grapheneos.deskclock.core.theme.DeskClockTheme

@Composable
fun AlarmPopUpTertiary(
    alarm: AlarmUiModel?,
    labelText: String,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    AlarmPopUpBase(
        alarm = alarm,
        labelText = labelText
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            if (alarm != null) {
                val minutes = alarm.snoozeDurationMinutes
                Text(
                    text = "${stringResource(R.string.snooze)}: ${
                        stringResource(
                            R.string.n_minutes,
                            minutes
                        )
                    }",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SwipeSlider(
                centerLabel = stringResource(R.string.swipe_to_dismiss),
                modifier = Modifier.fillMaxWidth(),
                leftAction = SwipeAction(
                    icon = Icons.Default.Snooze,
                    label = stringResource(R.string.snooze),
                    color = { MaterialTheme.colorScheme.secondary },
                    onTrigger = onSnooze
                ),
                rightAction = SwipeAction(
                    icon = Icons.Default.Check,
                    label = stringResource(R.string.dismiss),
                    color = { MaterialTheme.colorScheme.tertiary },
                    onTrigger = onDismiss
                )
            )
        }
    }
}

@Preview
@Composable
fun AlarmPopUpTertiaryPreview() {
    DeskClockTheme {
        AlarmPopUpTertiary(
            alarm = null,
            labelText = stringResource(R.string.wake_up),
            onDismiss = {},
            onSnooze = {}
        )
    }
}
