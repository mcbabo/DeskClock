package app.grapheneos.deskclock.stopwatch.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.core.presentation.components.groupitems.GroupRow
import app.grapheneos.deskclock.stopwatch.data.Lap
import app.grapheneos.deskclock.stopwatch.util.formatLapTime

@Composable
fun LapListItem(
    lap: Lap,
    modifier: Modifier = Modifier
) {
    GroupRow(
        modifier = modifier.fillMaxWidth(),
        content = {
            Text(
                text = "${stringResource(R.string.lap)} ${lap.number}",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = "+${formatLapTime(lap.splitMillis)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        },
        trailingContent = {
            Text(
                text = formatLapTime(lap.totalMillis),
                style = MaterialTheme.typography.titleMedium
            )
        }
    )
}
