package app.grapheneos.deskclock.alarm.presentation.popup.styles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.alarm.presentation.AlarmUiModel
import app.grapheneos.deskclock.core.presentation.PopUpButton
import app.grapheneos.deskclock.core.presentation.SwipeAction
import app.grapheneos.deskclock.core.presentation.SwipeSlider
import app.grapheneos.deskclock.core.theme.DeskClockTheme
import app.grapheneos.deskclock.core.util.Constants

@Composable
fun AlarmPopUpVariant(
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
            val minutes = alarm?.snoozeDurationMinutes ?: Constants.Alarm.DEFAULT_SNOOZE_TIME
            PopUpButton(
                onClick = onSnooze,
                icon = Icons.Default.Snooze,
                text = stringResource(R.string.snooze),
                bottomText = stringResource(R.string.n_minutes, minutes),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )

            SwipeSlider(
                centerLabel = stringResource(R.string.swipe_to_dismiss),
                modifier = Modifier.fillMaxWidth(),
                leftAction = SwipeAction(
                    icon = Icons.Default.Close,
                    label = stringResource(R.string.dismiss),
                    color = { MaterialTheme.colorScheme.tertiary },
                    onTrigger = onDismiss
                ),
                rightAction = SwipeAction(
                    icon = Icons.Default.Close,
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
fun AlarmPopUpVariantPreview() {
    DeskClockTheme {
        AlarmPopUpVariant(
            alarm = null,
            labelText = stringResource(R.string.wake_up),
            onDismiss = {},
            onSnooze = {}
        )
    }
}
