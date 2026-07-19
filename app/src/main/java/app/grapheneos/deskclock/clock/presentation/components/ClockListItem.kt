package app.grapheneos.deskclock.clock.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.clock.presentation.ClockUiModel
import app.grapheneos.deskclock.core.presentation.components.GroupItem
import app.grapheneos.deskclock.core.presentation.components.GroupRow
import app.grapheneos.deskclock.core.theme.DeskClockTheme
import app.grapheneos.deskclock.core.util.formatSystemTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockListItem(
    display: ClockUiModel,
    index: Int,
    listSize: Int,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    val dayLabel = stringResource(display.dayResId)
    val hourLabel = if (display.hoursDiff == 0L) {
        stringResource(R.string.same_time)
    } else {
        "${if (display.hoursDiff > 0) "+" else ""}${display.hoursDiff}h"
    }

    GroupItem(
        index = index,
        count = listSize,
        onClick = { }
    ) {
        GroupRow(
            content = {
                Text(
                    text = display.cityName,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            supportingContent = {
                Text(
                    text = "$dayLabel, $hourLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            },
            trailingContent = {
                Text(
                    text = formatSystemTime(context, display.hours, display.minutes),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.displaySmall,
                )
            },
            verticalAlignment = Alignment.CenterVertically
        )
    }
}

@Preview
@Composable
fun ClockListItemPreview() {
    DeskClockTheme {
        Surface {
            ClockListItem(
                ClockUiModel(
                    zoneId = ZoneId.systemDefault(),
                    cityName = "Vienna",
                    hours = 8,
                    minutes = 30,
                    dayResId = R.string.yesterday,
                    hoursDiff = 4L
                ),
                0,
                1
            ) { }
        }
    }
}
