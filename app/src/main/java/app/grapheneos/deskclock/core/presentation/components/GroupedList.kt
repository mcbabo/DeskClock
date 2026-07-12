package app.grapheneos.deskclock.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.core.theme.DeskClockTheme

@Composable
fun GroupItem(
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = getGroupedShapes(index, count),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
fun GroupRow(
    modifier: Modifier = Modifier,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    ListItem(
        modifier = modifier,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        supportingContent = supportingContent,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        content = content
    )
}

@Composable
fun ListGroup(
    title: String? = null, content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp), content = content
        )
    }
}


fun getGroupedShapes(index: Int, count: Int, cornerRadius: Dp = 20.dp): Shape {
    val flat = 4.dp
    return when {
        count <= 1 -> RoundedCornerShape(cornerRadius)
        index == 0 -> RoundedCornerShape(
            topStart = cornerRadius, topEnd = cornerRadius, bottomStart = flat, bottomEnd = flat
        )

        index == count - 1 -> RoundedCornerShape(
            topStart = flat, topEnd = flat, bottomStart = cornerRadius, bottomEnd = cornerRadius
        )

        else -> RoundedCornerShape(flat)
    }
}

@Preview
@Composable
fun GroupedListPreview() {
    DeskClockTheme {
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column {
                    Text(
                        text = "Alarm Configuration",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )

                    val items = 3
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        GroupItem(index = 0, count = items, onClick = {}) {
                            GroupRow(
                                content = { Text("Alarm Label") },
                                supportingContent = { Text("Morning Wakeup") },
                                leadingContent = { Icon(Icons.AutoMirrored.Filled.Label, null) },
                                trailingContent = {
                                    Text(
                                        "Work", color = MaterialTheme.colorScheme.primary
                                    )
                                })
                        }

                        GroupItem(index = 1, count = items, onClick = {}) {
                            GroupRow(
                                content = { Text("Ringtone") },
                                leadingContent = { Icon(Icons.Default.NotificationsActive, null) },
                                trailingContent = { Text("Cesium") })
                        }

                        GroupItem(index = 2, count = items) {
                            GroupRow(
                                content = { Text("Vibrate") },
                                leadingContent = { Icon(Icons.Default.Vibration, null) },
                                trailingContent = { Switch(checked = true, onCheckedChange = {}) })
                        }
                    }
                }

                Column {
                    Text(
                        text = "Actions",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )

                    GroupItem(index = 0, count = 1, onClick = {}) {
                        GroupRow(content = { Text("Delete Alarm") }, leadingContent = {
                            Icon(
                                Icons.Default.Delete,
                                null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        })
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    val simpleItems = 2
                    repeat(simpleItems) { i ->
                        GroupItem(index = i, count = simpleItems, onClick = {}) {
                            GroupRow(content = { Text("Simple Item ${i + 1}") })
                        }
                    }

                    repeat(simpleItems) { i ->
                        GroupItem(index = i, count = simpleItems, onClick = {}) {
                            GroupRow(
                                content = { Text("Simple Item ${i + 1}") },
                                leadingContent = { Avatar("S") })
                        }
                    }
                }
            }
        }
    }
}
