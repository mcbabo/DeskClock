package app.grapheneos.deskclock.alarm.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.alarm.data.AlarmEntity
import app.grapheneos.deskclock.alarm.data.AlarmWithInstance
import app.grapheneos.deskclock.alarm.util.AlarmDayFormatter
import app.grapheneos.deskclock.core.theme.DeskClockTheme

@Composable
fun AlarmListItem(
    alarmWithInstance: AlarmWithInstance,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val alarm = alarmWithInstance.alarm
    val context = LocalContext.current

    val timeText = String.format(
        LocalLocale.current.platformLocale,
        "%02d:%02d",
        alarm.hour,
        alarm.minute
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(timeText, style = MaterialTheme.typography.displayLarge)
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
        }
        Switch(
            checked = alarm.isEnabled,
            onCheckedChange = onToggle
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
                {},
                {}
            )
        }
    }
}
