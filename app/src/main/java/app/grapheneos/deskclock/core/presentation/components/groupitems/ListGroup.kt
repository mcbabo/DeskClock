package app.grapheneos.deskclock.core.presentation.components.groupitems

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * DSL scope used inside [ListGroup] to declare each row via [item], without
 * exposing the underlying list collection to callers.
 */
interface ListGroupScope {
    fun item(
        onClick: (() -> Unit)? = null,
        enabled: Boolean = true,
        content: @Composable () -> Unit
    )
}

/**
 * An eager (non-lazy) group of rows rendered as [GroupItem]s inside a
 * [Column], with an optional [title] header. Use this for a fixed, known set
 * of rows (e.g. a settings section or an alarm's editor drawer). For large
 * or dynamic lists inside a `LazyColumn`, use [lazyGroup] instead.
 */
@Composable
fun ListGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: ListGroupScope.() -> Unit
) {
    data class ItemInfo(
        val onClick: (() -> Unit)?,
        val enabled: Boolean,
        val content: @Composable () -> Unit
    )

    val itemData = remember(content) {
        mutableListOf<ItemInfo>()
    }
    val scope = remember(itemData) {
        object : ListGroupScope {
            override fun item(
                onClick: (() -> Unit)?,
                enabled: Boolean,
                content: @Composable () -> Unit
            ) {
                itemData.add(ItemInfo(onClick, enabled, content))
            }
        }
    }
    itemData.clear()
    scope.content()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        if (title != null) {
            GroupTitle(title)
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            itemData.forEachIndexed { index, item ->
                GroupItem(
                    index = index,
                    count = itemData.size,
                    onClick = item.onClick,
                    enabled = item.enabled,
                    content = item.content
                )
            }
        }
    }
}
