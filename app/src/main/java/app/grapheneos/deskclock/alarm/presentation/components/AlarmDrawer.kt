package app.grapheneos.deskclock.alarm.presentation.components

import android.net.Uri
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import app.grapheneos.deskclock.alarm.presentation.AlarmIntent
import app.grapheneos.deskclock.alarm.presentation.AlarmUiModel
import app.grapheneos.deskclock.core.presentation.Layout
import app.grapheneos.deskclock.core.presentation.components.DismissKeyboard
import app.grapheneos.deskclock.core.presentation.components.groupitems.InlineEditableValueGroupRow
import app.grapheneos.deskclock.core.presentation.components.groupitems.ListGroup
import app.grapheneos.deskclock.core.presentation.components.groupitems.SwitchGroupRow
import app.grapheneos.deskclock.core.presentation.components.groupitems.ValueGroupRow
import app.grapheneos.deskclock.core.presentation.screenPadding
import app.grapheneos.deskclock.core.ringtone.RingtoneItem
import app.grapheneos.deskclock.core.theme.DeskClockTheme
import app.grapheneos.deskclock.core.util.formatSystemTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDrawer(
    alarm: AlarmUiModel,
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
                alarm = alarm,
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
    alarm: AlarmUiModel,
    ringtones: List<RingtoneItem>,
    onDismissRequest: () -> Unit,
    onIntent: (AlarmIntent) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    var localAlarm by remember(alarm.id) {
        mutableStateOf(alarm)
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
        AlarmDrawerTimeHeader(
            hour = localAlarm.hour,
            minute = localAlarm.minute,
            onEditClick = {
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                showTimePicker = true
            }
        )

        AlarmDrawerDaySelection(
            daysOfWeek = localAlarm.daysOfWeek,
            onDaysChange = { newBitmask ->
                localAlarm = localAlarm.copy(daysOfWeek = newBitmask)
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
        )

        AlarmDrawerOptionsSection(
            alarm = localAlarm,
            selectedRingtoneName = selectedRingtoneName,
            onAlarmChange = { localAlarm = it },
            onRingtoneClick = { showRingtonePicker = true }
        )

        Spacer(modifier.height(Layout.ScreenVertical))

        AlarmDrawerActionButtons(
            onDelete = {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                onDelete()
            },
            onSave = {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                onIntent(AlarmIntent.UpdateAlarm(localAlarm))
                onDismissRequest()
            }
        )
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

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            onIntent(AlarmIntent.ImportRingtone(it))
        }
    }

    if (showRingtonePicker) {
        RingtonePickerDialog(
            uiState = RingtonePickerUiState(
                ringtones = ringtones,
                selectedUri = localAlarm.ringtoneUri,
                isDirectBootOnly = false
            ),
            onPlayPreview = { uri -> onIntent(AlarmIntent.PlayPreview(uri)) },
            onStopPreview = { onIntent(AlarmIntent.StopPreview) },
            onImportRingtone = {
                picker.launch(arrayOf("audio/*"))
            },
            onDeleteRingtone = { ringtone ->
                onIntent(AlarmIntent.DeleteCustomRingtone(ringtone))
            },
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

@Composable
private fun AlarmDrawerTimeHeader(
    hour: Int,
    minute: Int,
    onEditClick: () -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatSystemTime(context, hour, minute),
            modifier = Modifier.padding(horizontal = 4.dp),
            style = MaterialTheme.typography.displayMedium
        )

        Button(
            onClick = onEditClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(stringResource(R.string.edit))
        }
    }
}

@Composable
private fun AlarmDrawerDaySelection(
    daysOfWeek: Int,
    onDaysChange: (Int) -> Unit
) {
    DayChips(
        selectedDaysBitmask = daysOfWeek,
        onBitmaskChange = onDaysChange
    )
}

@Composable
private fun AlarmDrawerOptionsSection(
    alarm: AlarmUiModel,
    selectedRingtoneName: String,
    onAlarmChange: (AlarmUiModel) -> Unit,
    onRingtoneClick: () -> Unit
) {
    ListGroup {
        item {
            SwitchGroupRow(
                label = stringResource(R.string.delete_after_use),
                checked = if (alarm.daysOfWeek == 0) {
                    alarm.deleteAfterUse
                } else {
                    false
                },
                onCheckedChange = { isChecked ->
                    onAlarmChange(alarm.copy(deleteAfterUse = isChecked))
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.delete)
                    )
                },
                enabled = alarm.daysOfWeek == 0
            )
        }
    }

    ListGroup {
        item {
            InlineEditableValueGroupRow(
                label = stringResource(R.string.name_of_alarm),
                value = alarm.label,
                onValueChange = { newLabel -> onAlarmChange(alarm.copy(label = newLabel)) },
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
                onClick = onRingtoneClick,
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
                checked = alarm.vibrate,
                onCheckedChange = {
                    onAlarmChange(alarm.copy(vibrate = it))
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
}

@Composable
private fun AlarmDrawerActionButtons(
    onDelete: () -> Unit,
    onSave: () -> Unit
) {
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
            onClick = onSave,
        ) {
            Text(stringResource(R.string.save))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmDrawerContentPreview() {
    DeskClockTheme {
        AlarmDrawerContent(
            alarm = AlarmUiModel(
                id = 1,
                hour = 7,
                minute = 30,
                daysOfWeek = 31,
                isEnabled = true,
                deleteAfterUse = false,
                label = "",
                ringtoneUri = Uri.EMPTY,
                vibrate = true,
                snoozeDurationMinutes = 10,
                timeText = "7:30",
                daysOfWeekText = "Mon-Fri"
            ),
            ringtones = emptyList(),
            onDismissRequest = {},
            onIntent = {},
            onDelete = {}
        )
    }
}
