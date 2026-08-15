package app.grapheneos.deskclock.alarm.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.alarm.presentation.components.AlarmDrawer
import app.grapheneos.deskclock.alarm.presentation.components.AlarmListItem
import app.grapheneos.deskclock.alarm.presentation.components.DialWithDialog
import app.grapheneos.deskclock.core.presentation.Layout
import app.grapheneos.deskclock.core.presentation.components.FloatingActionButton
import app.grapheneos.deskclock.core.presentation.components.groupitems.lazyGroup
import app.grapheneos.deskclock.core.theme.DeskClockTheme
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    uiState: AlarmUiState,
    onIntent: (AlarmIntent) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    triggerAdd: Boolean = false,
    onAddTriggered: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    val alarmDeletedText = stringResource(R.string.alarm_deleted)
    val undoText = stringResource(R.string.undo)

    var editingAlarmId by remember { mutableStateOf<Long?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(triggerAdd) {
        if (triggerAdd) {
            showTimePicker = true
            onAddTriggered()
        }
    }

    val editingAlarm = remember(editingAlarmId, uiState.alarms) {
        uiState.alarms.find { it.id == editingAlarmId }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.tab_alarm))
                        uiState.nextAlarmRemainingTime?.let {
                            Text(
                                text = stringResource(R.string.next_alarm_in, it),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                text = { Text(text = stringResource(R.string.add_alarm)) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_alarm)
                    )
                },
                onClick = { showTimePicker = true }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.alarms.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_alarms_yet),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(Layout.GroupedList.ItemSpacing),
                    contentPadding = Layout.contentPadding()
                ) {
                    lazyGroup(
                        items = uiState.alarms,
                        key = { it.id },
                        onClick = { editingAlarmId = it.id }
                    ) { alarmUiModel ->
                        AlarmListItem(
                            alarm = alarmUiModel,
                            onToggle = { onIntent(AlarmIntent.ToggleAlarm(alarmUiModel)) },
                            onClick = { editingAlarmId = alarmUiModel.id }
                        )
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        val now = Calendar.getInstance()
        DialWithDialog(
            initialHour = now.get(Calendar.HOUR_OF_DAY),
            initialMinute = now.get(Calendar.MINUTE),
            onConfirm = { pickerState ->
                onIntent(
                    AlarmIntent.AddAlarm(
                        hour = pickerState.hour,
                        minute = pickerState.minute,
                        daysOfWeek = 0,
                        deleteAfterUse = false,
                        label = ""
                    )
                )
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    editingAlarm?.let { alarmUiModel ->
        AlarmDrawer(
            alarm = alarmUiModel,
            ringtones = uiState.ringtones,
            onDismissRequest = {
                onIntent(AlarmIntent.StopPreview)
                editingAlarmId = null
            },
            onIntent = onIntent,
            onDelete = {
                onIntent(AlarmIntent.DeleteAlarm(alarmUiModel))
                editingAlarmId = null

                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = alarmDeletedText,
                        actionLabel = undoText,
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        onIntent(AlarmIntent.RestoreAlarm(alarmUiModel))
                    }
                }
            }
        )
    }
}

@Preview
@Composable
fun AlarmScreenPreview() {
    DeskClockTheme {
        AlarmScreen(
            uiState = AlarmUiState(
                alarms = listOf(
                    AlarmUiModel(
                        id = 1,
                        hour = 7,
                        minute = 30,
                        daysOfWeek = 31,
                        isEnabled = true,
                        deleteAfterUse = false,
                        label = "Wake up",
                        ringtoneUri = "",
                        vibrate = true,
                        snoozeDurationMinutes = 10,
                        timeText = "7:30",
                        daysOfWeekText = "Mon-Fri"
                    ),
                    AlarmUiModel(
                        id = 2,
                        hour = 16,
                        minute = 0,
                        daysOfWeek = 62,
                        isEnabled = false,
                        deleteAfterUse = false,
                        label = "Gym",
                        ringtoneUri = "",
                        vibrate = true,
                        snoozeDurationMinutes = 10,
                        timeText = "16:00",
                        daysOfWeekText = "Sat-Sun"
                    )
                ),
                nextAlarmRemainingTime = "7h 30m"
            ),
            onIntent = {},
            onSettingsClick = {}
        )
    }
}
