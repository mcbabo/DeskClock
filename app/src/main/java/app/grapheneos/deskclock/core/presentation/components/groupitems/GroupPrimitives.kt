package app.grapheneos.deskclock.core.presentation.components.groupitems

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemDefaults.verticalAlignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.core.presentation.Layout

/**
 * A single card-like surface used as the visual container for one row in a
 * grouped list. [index] and [count] determine which corners get rounded
 * (top/middle/bottom of the group), via [Layout.getGroupedShapes].
 */
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

/**
 * A single row inside a [GroupItem], built on top of [ListItem]. This is the
 * base layout primitive that all the prebuilt row variants below
 * ([ValueGroupRow], [SwitchGroupRow], [InlineEditableValueGroupRow]) are
 * built from. Kept free of any switch/value/navigation semantics so it stays
 * reusable for one-off custom rows.
 */
@Composable
fun GroupRow(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    verticalAlignment: Alignment.Vertical = verticalAlignment(),
    content: @Composable () -> Unit
) {
    ListItem(
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        supportingContent = supportingContent,
        verticalAlignment = verticalAlignment,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        content = content
    )
}

/**
 * Section header text shared by both [ListGroup] and
 * [LazyListScope.lazyGroup] / [LazyListScope.groupHeader], so both APIs
 * render identical-looking headers from one definition.
 */
@Composable
fun GroupTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 8.dp)
    )
}
