package app.grapheneos.deskclock.alarm.presentation.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.alarm.data.AlarmEntity
import app.grapheneos.deskclock.alarm.data.AlarmWithInstance
import app.grapheneos.deskclock.alarm.presentation.AlarmAction
import app.grapheneos.deskclock.core.theme.DeskClockTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDrawer(
    key: Long,
    alarmWithInstance: AlarmWithInstance,
    onDismissRequest: () -> Unit,
    onIntent: (AlarmAction) -> Unit,
    onDelete: () -> Unit
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
        AlarmDrawerContent(
            alarmWithInstance = alarmWithInstance,
            onDismissRequest = onDismissRequest,
            onIntent = onIntent,
            onDelete = onDelete
        )
    }
}

@Composable
fun AlarmDrawerContent(
    alarmWithInstance: AlarmWithInstance,
    onDismissRequest: () -> Unit,
    onIntent: (AlarmAction) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var localAlarm by remember(alarmWithInstance.alarm.id) {
        mutableStateOf(alarmWithInstance.alarm)
    }

    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = String.format(
                    LocalLocale.current.platformLocale,
                    "%02d:%02d",
                    localAlarm.hour,
                    localAlarm.minute
                ),
                modifier = Modifier.padding(horizontal = 4.dp),
                style = MaterialTheme.typography.displayMedium
            )

            Button(
                onClick = {
                    showTimePicker = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(stringResource(R.string.edit))
            }
        }

        DayChips(
            selectedDaysBitmask = localAlarm.daysOfWeek,
            onBitmaskChange = { newBitmask ->
                localAlarm = localAlarm.copy(daysOfWeek = newBitmask)
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
        )

        GroupedList(
            listOf(
                GroupedListItem(
                    modifier = Modifier.alpha(
                        if (localAlarm.daysOfWeek == 0) {
                            1f
                        } else {
                            0.38f
                        }
                    ),
                    content = {
                        Text(
                            text = stringResource(R.string.delete_after_use),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete)
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = if (localAlarm.daysOfWeek == 0) {
                                localAlarm.deleteAfterUse
                            } else {
                                false
                            },
                            onCheckedChange = { isChecked ->
                                localAlarm = localAlarm.copy(deleteAfterUse = isChecked)
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            },
                            enabled = localAlarm.daysOfWeek == 0
                        )
                    }
                )
            )
        )

        Spacer(modifier.height(0.dp))

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
                            contentDescription = stringResource(R.string.name_of_alarm)
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
                            contentDescription = stringResource(R.string.ringtone)
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
                            checked = localAlarm.vibrate,
                            onCheckedChange = { isChecked ->
                                localAlarm = localAlarm.copy(vibrate = isChecked)
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            }
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

        Spacer(modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Text(stringResource(R.string.delete))
            }
            Button(
                onClick = {
                    onIntent(AlarmAction.UpdateAlarm(localAlarm))
                    onDismissRequest()
                },
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }

    if (showTimePicker) {
        DialWithDialog(
            initialHour = localAlarm.hour,
            initialMinute = localAlarm.minute,
            onConfirm = { pickerState ->
                localAlarm = localAlarm.copy(
                    hour = pickerState.hour,
                    minute = pickerState.minute
                )
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmDrawerContentPreview() {
    DeskClockTheme {
        AlarmDrawerContent(
            AlarmWithInstance(
                alarm = AlarmEntity(
                    id = 1,
                    hour = 7,
                    minute = 30,
                    daysOfWeek = 0,
                    isEnabled = true,
                    deleteAfterUse = false,
                    label = ""
                ),
                instance = null
            ),
            onDismissRequest = {},
            onIntent = {},
            onDelete = {}
        )
    }
}
