package app.grapheneos.deskclock.settings.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.core.presentation.Layout
import app.grapheneos.deskclock.core.presentation.components.groupitems.GroupRow
import app.grapheneos.deskclock.core.presentation.components.groupitems.ListGroup
import app.grapheneos.deskclock.core.presentation.components.groupitems.SwitchGroupRow
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
                                contentDescription = stringResource(themeMode.displayNameRes)
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
                        content = {
                            Text(text = stringResource(themeMode.displayNameRes))
                        },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RingtoneVolumeDrawer(
    customRingtoneVolumeEnabled: Boolean,
    currentVolume: Float,
    onChange: (Boolean, Float) -> Unit,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )

    var enabled by remember { mutableStateOf(customRingtoneVolumeEnabled) }

    // Add a haptic feedback on value change maybe
    val sliderState = rememberSliderState(
        value = currentVolume,
        steps = 9,
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .screenPadding(),
            verticalArrangement = Arrangement.spacedBy(Layout.ScreenVertical)
        ) {
            ListGroup {
                item {
                    SwitchGroupRow(
                        label = stringResource(R.string.settings_custom_volume),
                        checked = enabled,
                        onCheckedChange = { newChecked ->
                            enabled = newChecked
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Tune,
                                contentDescription = stringResource(R.string.settings_custom_volume)
                            )
                        }
                    )
                }

                item {
                    Slider(
                        state = sliderState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .screenPadding(),
                        enabled = enabled,
                        track = {
                            SliderDefaults.Track(
                                modifier = Modifier.height(36.dp),
                                trackCornerSize = 12.dp,
                                sliderState = sliderState,
                            )
                        },
                    )
                }

                item {
                    GroupRow(
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = stringResource(R.string.tip)
                            )
                        },
                        supportingContent = {
                            Text(stringResource(R.string.settings_custom_volume_tip))
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.tip))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { onChange(enabled, sliderState.value) }
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}
