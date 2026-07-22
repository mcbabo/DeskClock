package app.grapheneos.deskclock.core.presentation.components.groupitems

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable

/**
 * Adds a header (if [title] is given) and a set of grouped, rounded-corner
 * [GroupItem] rows to a `LazyColumn`, one per entry in [items]. The lazy
 * counterpart to [ListGroup], for dynamic or large lists (e.g. a timezone
 * picker or alarm list) where items are recycled rather than composed eagerly.
 */
fun <T> LazyListScope.lazyGroup(
    items: List<T>,
    title: String? = null,
    key: ((item: T) -> Any)? = null,
    onClick: (T) -> Unit = {},
    itemContent: @Composable (T) -> Unit
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
            itemContent(item)
        }
    }
}

/**
 * Renders a single [GroupItem] as its own standalone "group" of one, for a
 * lone row inside a `LazyColumn` that isn't part of a larger set (e.g. a
 * single "Delete" action row).
 */
fun LazyListScope.standaloneGroupItem(
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    item {
        GroupItem(index = 0, count = 1, onClick = onClick) {
            content()
        }
    }
}

/**
 * Renders a section header inside a `LazyColumn`, matching [ListGroup]'s
 * header styling. Exposed separately from [lazyGroup] so a header can be
 * placed above a manually-built sequence of items (e.g. when rows need
 * custom layout, like a leading delete icon next to a [GroupItem]).
 */
fun LazyListScope.groupHeader(title: String, key: Any? = null) {
    item(key = key) {
        GroupTitle(title)
    }
}
