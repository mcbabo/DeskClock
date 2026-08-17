package app.grapheneos.deskclock.alarm.presentation.components

import android.net.Uri
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.alarm.presentation.AlarmUiModel
import app.grapheneos.deskclock.core.presentation.components.groupitems.GroupRow
import app.grapheneos.deskclock.core.theme.DeskClockTheme

@Composable
fun AlarmListItem(
    alarm: AlarmUiModel,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val view = LocalView.current

    GroupRow(
        modifier = Modifier,
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            onClick()
        },
        content = {
            Text(
                text = alarm.timeText,
                style = MaterialTheme.typography.displayMedium,
            )
        },
        supportingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alarm.label.ifBlank { stringResource(R.string.alarm) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = alarm.daysOfWeekText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = { newChecked ->
                    view.performHapticFeedback(
                        if (newChecked) {
                            HapticFeedbackConstants.TOGGLE_ON
                        } else {
                            HapticFeedbackConstants.TOGGLE_OFF
                        }
                    )
                    onToggle(newChecked)
                }
            )
        },
        verticalAlignment = Alignment.CenterVertically
    )
}

@Preview
@Composable
fun AlarmListItemPreview() {
    DeskClockTheme {
        Surface {
            AlarmListItem(
                alarm = AlarmUiModel(
                    id = 1,
                    hour = 2,
                    minute = 30,
                    daysOfWeek = 62,
                    isEnabled = true,
                    deleteAfterUse = false,
                    label = "",
                    ringtoneUri = Uri.EMPTY,
                    vibrate = true,
                    snoozeDurationMinutes = 10,
                    timeText = "2:30",
                    daysOfWeekText = "Sat-Sun"
                ),
                onToggle = {},
                onClick = {}
            )
        }
    }
}
