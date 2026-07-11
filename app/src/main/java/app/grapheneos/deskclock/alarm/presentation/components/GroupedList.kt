package app.grapheneos.deskclock.alarm.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.core.theme.DeskClockTheme

data class GroupedListItem(
    val modifier: Modifier = Modifier,
    val leadingContent: @Composable () -> Unit,
    val content: @Composable () -> Unit,
    val trailingContent: @Composable () -> Unit,
)

@Composable
fun GroupedList(items: List<GroupedListItem>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items.forEachIndexed { index, item ->
            Surface(
                shape = getGroupedShape(index = index, count = items.size),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    modifier = item.modifier,
                    leadingContent = item.leadingContent,
                    trailingContent = item.trailingContent,
                    overlineContent = null,
                    supportingContent = null,
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    elevation = ListItemDefaults.elevation(),
                    content = item.content,
                )
            }
        }
    }
}

fun getGroupedShape(index: Int, count: Int, cornerRadius: Dp = 16.dp): Shape {
    val defaultCorner = 4.dp

    return when {
        count == 1 -> RoundedCornerShape(cornerRadius)

        index == 0 -> RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius,
            bottomStart = defaultCorner,
            bottomEnd = defaultCorner
        )

        index == count - 1 -> RoundedCornerShape(
            topStart = defaultCorner,
            topEnd = defaultCorner,
            bottomStart = cornerRadius,
            bottomEnd = cornerRadius
        )

        else -> RoundedCornerShape(defaultCorner)
    }
}

@Preview
@Composable
fun GroupedListPreview() {
    DeskClockTheme {
        GroupedList(
            listOf(
                GroupedListItem(
                    content = {
                        Text(
                            text = stringResource(R.string.name_of_alarm),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Label,
                            contentDescription = ""
                        )
                    },
                    trailingContent = {
                        Text(
                            text = stringResource(R.string.alarm),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                ),
                GroupedListItem(
                    content = {
                        Text(
                            text = stringResource(R.string.ringtone),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = ""
                        )
                    },
                    trailingContent = {
                        Text(
                            text = "Standard (Cesium)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                ),
                GroupedListItem(
                    content = {
                        Text(
                            text = stringResource(R.string.vibrate),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = true,
                            onCheckedChange = { }
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = ""
                        )
                    }
                )
            )
        )
    }
}
