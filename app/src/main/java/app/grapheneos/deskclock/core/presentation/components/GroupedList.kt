package app.grapheneos.deskclock.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemDefaults.verticalAlignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.core.presentation.Layout
import app.grapheneos.deskclock.core.presentation.screenPadding
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
        shape = Layout.getGroupedShapes(index, count),
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
    verticalAlignment: Alignment.Vertical = verticalAlignment(),
    content: @Composable () -> Unit
) {
    ListItem(
        modifier = modifier,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        supportingContent = supportingContent,
        verticalAlignment = verticalAlignment,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        content = content
    )
}

@Composable
fun ListGroup(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 8.dp)
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            content = content
        )
    }
}

/**
 * A logical wrapper that adds a header and a set of grouped items to a LazyColumn.
 */
fun <T> LazyListScope.lazyGroup(
    items: List<T>,
    title: String? = null,
    key: ((item: T) -> Any)? = null,
    onClick: (T) -> Unit = {},
    itemContent: @Composable (Int, T) -> Unit
) {
    if (title != null) {
        groupHeader(title)
    }

    itemsIndexed(
        items = items,
        key = if (key != null) { _, item -> key(item) } else null
    ) { index, item ->
        GroupItem(
            index = index,
            count = items.size,
            onClick = { onClick(item) }
        ) {
            itemContent(index, item)
        }
    }
}

/**
 * Extension for a single standalone item
 */
fun LazyListScope.standaloneGroupItem(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    item {
        GroupItem(index = 0, count = 1, onClick = onClick) {
            content()
        }
    }
}

/**
 * Extension for the Header
 */
fun LazyListScope.groupHeader(title: String) {
    item {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 8.dp)
        )
    }
}

@Preview
@Composable
fun GroupedListPreview() {
    DeskClockTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.screenPadding(),
                verticalArrangement = Arrangement.spacedBy(Layout.SectionSpacing)
            ) {
                val items = 3
                ListGroup(
                    "Alarm Configuration",
                    {
                        Column(verticalArrangement = Arrangement.spacedBy(Layout.GroupedList.ItemSpacing)) {
                            GroupItem(index = 0, count = items, onClick = {}) {
                                GroupRow(
                                    content = { Text("Alarm Label") },
                                    supportingContent = { Text("Morning Wakeup") },
                                    leadingContent = {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Label,
                                            null
                                        )
                                    },
                                    trailingContent = {
                                        Text(
                                            "Work",
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )
                            }

                            GroupItem(index = 1, count = items, onClick = {}) {
                                GroupRow(
                                    content = { Text("Ringtone") },
                                    leadingContent = {
                                        Icon(
                                            Icons.Default.NotificationsActive,
                                            null
                                        )
                                    },
                                    trailingContent = { Text("Cesium") }
                                )
                            }

                            GroupItem(index = 2, count = items) {
                                GroupRow(
                                    content = { Text("Vibrate") },
                                    leadingContent = { Icon(Icons.Default.Vibration, null) },
                                    trailingContent = {
                                        Switch(
                                            checked = true,
                                            onCheckedChange = {}
                                        )
                                    }
                                )
                            }
                        }
                    }
                )

                ListGroup(
                    "Actions",
                    {
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
                )

                ListGroup(
                    "Basic items",
                    {
                        repeat(2) { i ->
                            GroupItem(index = i, count = 2, onClick = {}) {
                                GroupRow(content = { Text("Simple Item ${i + 1}") })
                            }
                        }
                    }
                )

                ListGroup(
                    "Items with avatar",
                    {
                        repeat(2) { i ->
                            GroupItem(index = i, count = 2, onClick = {}) {
                                GroupRow(
                                    content = { Text("Simple Item ${i + 1}") },
                                    leadingContent = { Avatar("S") }
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}
