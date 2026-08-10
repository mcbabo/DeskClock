package app.grapheneos.deskclock.alarm.presentation.popup.styles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import app.grapheneos.deskclock.alarm.util.AlarmConstants
import app.grapheneos.deskclock.core.presentation.PopUpButton
import app.grapheneos.deskclock.core.theme.DeskClockTheme

@Composable
fun AlarmPopUpDefault(
    alarm: AlarmUiModel?,
    labelText: String,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    AlarmPopUpBase(
        alarm = alarm,
        labelText = labelText
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val minutes = alarm?.snoozeDurationMinutes ?: AlarmConstants.DEFAULT_SNOOZE_TIME
            PopUpButton(
                onClick = onSnooze,
                icon = Icons.Default.Snooze,
                text = stringResource(R.string.snooze),
                bottomText = stringResource(R.string.n_minutes, minutes),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )

            PopUpButton(
                onClick = onDismiss,
                icon = Icons.Default.Close,
                text = stringResource(R.string.dismiss),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Preview
@Composable
fun AlarmPopUpDefaultPreview() {
    DeskClockTheme {
        AlarmPopUpDefault(
            alarm = null,
            labelText = stringResource(R.string.wake_up),
            onDismiss = {},
            onSnooze = {}
        )
    }
}
