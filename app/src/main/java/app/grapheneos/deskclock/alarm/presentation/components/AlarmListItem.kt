package app.grapheneos.deskclock.alarm.presentation.components

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.alarm.data.AlarmEntity
import app.grapheneos.deskclock.alarm.data.AlarmWithInstance
import app.grapheneos.deskclock.alarm.util.AlarmDayFormatter
import app.grapheneos.deskclock.core.presentation.components.GroupItem
import app.grapheneos.deskclock.core.presentation.components.GroupRow
import app.grapheneos.deskclock.core.theme.DeskClockTheme
import app.grapheneos.deskclock.core.util.formatSystemTime

@Composable
fun AlarmListItem(
    alarmWithInstance: AlarmWithInstance,
    index: Int,
    listSize: Int,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val alarm = alarmWithInstance.alarm

    val timeText = formatSystemTime(context, alarm.hour, alarm.minute)

    GroupItem(
        index = index,
        count = listSize,
        onClick = onClick
    ) {
        GroupRow(
            content = {
                Text(
                    text = timeText,
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
                        text = AlarmDayFormatter.formatDaysOfWeek(context, alarm.daysOfWeek),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            trailingContent = {
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle
                )
            },
            verticalAlignment = Alignment.CenterVertically
        )
    }
}

@Preview
@Composable
fun AlarmListItemPreview() {
    DeskClockTheme {
        Surface {
            AlarmListItem(
                alarmWithInstance = AlarmWithInstance(
                    alarm = AlarmEntity(
                        id = 1,
                        hour = 2,
                        minute = 30,
                        daysOfWeek = 62,
                        isEnabled = true,
                    ),
                    instance = null
                ),
                0,
                1,
                {},
                {}
            )
        }
    }
}
