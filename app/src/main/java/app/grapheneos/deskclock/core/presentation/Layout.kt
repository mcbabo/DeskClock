package app.grapheneos.deskclock.core.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Layout {
    val ScreenHorizontal = 16.dp
    val ScreenVertical = 16.dp
    val SectionSpacing = 24.dp
    val FabBottomClearance = 88.dp
    val AvatarSize = 32.dp
    val ChipSpacing = 8.dp
    val RoundedCornerRadius = 20.dp

    object GroupedList {
        val CornerRadius = 20.dp
        val ItemSpacing = 2.dp
    }

    @Composable
    fun contentPadding(
        innerPadding: PaddingValues = PaddingValues(0.dp),
        extraBottom: Dp = FabBottomClearance
    ): PaddingValues {
        val layoutDirection = LocalLayoutDirection.current
        return PaddingValues(
            start = ScreenHorizontal + innerPadding.calculateStartPadding(layoutDirection),
            end = ScreenHorizontal + innerPadding.calculateEndPadding(layoutDirection),
            top = ScreenVertical + innerPadding.calculateTopPadding(),
            bottom = extraBottom + innerPadding.calculateBottomPadding()
        )
    }

    fun getGroupedShapes(
        index: Int,
        count: Int,
        cornerRadius: Dp = GroupedList.CornerRadius
    ): Shape {
        val flat = 4.dp
        return when {
            count <= 1 -> RoundedCornerShape(cornerRadius)
            index == 0 -> RoundedCornerShape(
                topStart = cornerRadius,
                topEnd = cornerRadius,
                bottomStart = flat,
                bottomEnd = flat
            )

            index == count - 1 -> RoundedCornerShape(
                topStart = flat,
                topEnd = flat,
                bottomStart = cornerRadius,
                bottomEnd = cornerRadius
            )

            else -> RoundedCornerShape(flat)
        }
    }
}

fun Modifier.screenPadding() = this.padding(
    horizontal = Layout.ScreenHorizontal,
    vertical = Layout.ScreenVertical
)
