package app.grapheneos.deskclock.alarm.presentation.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.alarm.data.AlarmEntity
import app.grapheneos.deskclock.alarm.data.AlarmWithInstance
import app.grapheneos.deskclock.alarm.presentation.AlarmIntent
import app.grapheneos.deskclock.alarm.presentation.RingtoneItem
import app.grapheneos.deskclock.core.presentation.Layout
import app.grapheneos.deskclock.core.presentation.components.DismissKeyboard
import app.grapheneos.deskclock.core.presentation.components.groupitems.InlineEditableValueGroupRow
import app.grapheneos.deskclock.core.presentation.components.groupitems.ListGroup
import app.grapheneos.deskclock.core.presentation.components.groupitems.SwitchGroupRow
import app.grapheneos.deskclock.core.presentation.components.groupitems.ValueGroupRow
import app.grapheneos.deskclock.core.presentation.screenPadding
import app.grapheneos.deskclock.core.theme.DeskClockTheme
import app.grapheneos.deskclock.core.util.formatSystemTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDrawer(
    alarmWithInstance: AlarmWithInstance,
    ringtones: List<RingtoneItem>,
    onDismissRequest: () -> Unit,
    onIntent: (AlarmIntent) -> Unit,
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
        DismissKeyboard {
            AlarmDrawerContent(
                alarmWithInstance = alarmWithInstance,
                ringtones = ringtones,
                onDismissRequest = onDismissRequest,
                onIntent = onIntent,
                onDelete = onDelete
            )
        }
    }
}

@Composable
fun AlarmDrawerContent(
    alarmWithInstance: AlarmWithInstance,
    ringtones: List<RingtoneItem>,
    onDismissRequest: () -> Unit,
    onIntent: (AlarmIntent) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current

    var localAlarm by remember(alarmWithInstance.alarm.id) {
        mutableStateOf(alarmWithInstance.alarm)
    }
    val defaultValue = stringResource(R.string.default_value)
    val selectedRingtoneName = remember(localAlarm.ringtoneUri, ringtones) {
        ringtones.find { it.uri == localAlarm.ringtoneUri }?.name ?: defaultValue
    }

    var showTimePicker by remember { mutableStateOf(false) }
    var showRingtonePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .screenPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatSystemTime(context, localAlarm.hour, localAlarm.minute),
                modifier = Modifier.padding(horizontal = 4.dp),
                style = MaterialTheme.typography.displayMedium
            )

            Button(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
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

        ListGroup {
            item {
                SwitchGroupRow(
                    label = stringResource(R.string.delete_after_use),
                    checked = if (localAlarm.daysOfWeek == 0) {
                        localAlarm.deleteAfterUse
                    } else {
                        false
                    },
                    onCheckedChange = { isChecked ->
                        localAlarm = localAlarm.copy(deleteAfterUse = isChecked)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.delete)
                        )
                    },
                    enabled = localAlarm.daysOfWeek == 0
                )
            }
        }

        ListGroup {
            item {
                InlineEditableValueGroupRow(
                    label = stringResource(R.string.name_of_alarm),
                    value = localAlarm.label,
                    onValueChange = { newLabel -> localAlarm = localAlarm.copy(label = newLabel) },
                    placeholder = stringResource(R.string.alarm),
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Outlined.Label,
                            contentDescription = stringResource(R.string.name_of_alarm)
                        )
                    }
                )
            }
            item {
                ValueGroupRow(
                    label = stringResource(R.string.ringtone),
                    value = selectedRingtoneName,
                    onClick = { showRingtonePicker = true },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.NotificationsActive,
                            contentDescription = stringResource(R.string.ringtone)
                        )
                    }
                )
            }
            item {
                SwitchGroupRow(
                    label = stringResource(R.string.vibrate),
                    checked = localAlarm.vibrate,
                    onCheckedChange = {
                        localAlarm = localAlarm.copy(vibrate = it)
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Vibration,
                            contentDescription = stringResource(R.string.vibrate)
                        )
                    }
                )
            }
        }

        Spacer(modifier.height(Layout.ScreenVertical))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onDelete()
                },
                colors = ButtonDefaults.buttonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Text(stringResource(R.string.delete))
            }
            Button(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onIntent(AlarmIntent.UpdateAlarm(localAlarm))
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
                    hour = pickerState.hour, minute = pickerState.minute
                )
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    if (showRingtonePicker) {
        RingtonePickerDialog(
            ringtones = ringtones,
            initialUri = localAlarm.ringtoneUri,
            onPlayPreview = { uri -> onIntent(AlarmIntent.PlayPreview(uri)) },
            onStopPreview = { onIntent(AlarmIntent.StopPreview) },
            onDismiss = {
                onIntent(AlarmIntent.StopPreview)
                showRingtonePicker = false
            },
            onConfirm = { newUri ->
                localAlarm = localAlarm.copy(ringtoneUri = newUri)
                showRingtonePicker = false
            }
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
                    daysOfWeek = 31,
                    isEnabled = true,
                    deleteAfterUse = false,
                    label = ""
                ),
                instance = null
            ),
            ringtones = emptyList(),
            onDismissRequest = {},
            onIntent = {},
            onDelete = {}
        )
    }
}
