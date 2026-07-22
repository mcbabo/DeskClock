package app.grapheneos.deskclock.settings.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.core.presentation.components.groupitems.GroupRow
import app.grapheneos.deskclock.core.presentation.components.groupitems.ListGroup
import app.grapheneos.deskclock.core.presentation.screenPadding
import app.grapheneos.deskclock.settings.data.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeDrawer(
    currentThemeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        ListGroup(
            modifier = Modifier
                .fillMaxWidth()
                .screenPadding()
        ) {
            ThemeMode.entries.forEach { themeMode ->
                item(onClick = { onThemeChange(themeMode) }) {
                    GroupRow(
                        leadingContent = {
                            Icon(
                                imageVector = themeMode.icon,
                                contentDescription = themeMode.displayName
                            )
                        },
                        trailingContent = {
                            if (currentThemeMode == themeMode) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null
                                )
                            }
                        },
                        content = { Text(themeMode.displayName) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnoozeDrawer(
    snoozeMinutes: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val predefinedSnoozeIntervals = listOf(2, 5, 10, 15, 20, 30)

    var selectedInterval by remember { mutableIntStateOf(snoozeMinutes) }

    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        ListGroup(
            modifier = Modifier
                .fillMaxWidth()
                .screenPadding()
        ) {
            predefinedSnoozeIntervals.forEach { interval ->
                item(onClick = { onConfirm(interval) }) {
                    GroupRow(
                        trailingContent = {
                            if (selectedInterval == interval) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null
                                )
                            }
                        },
                        content = {
                            Text(text = stringResource(R.string.n_minutes, interval))
                        }
                    )
                }
            }
        }
    }
}
