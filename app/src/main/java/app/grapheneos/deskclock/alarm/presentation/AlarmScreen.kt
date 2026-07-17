package app.grapheneos.deskclock.alarm.presentation

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.alarm.data.AlarmWithInstance
import app.grapheneos.deskclock.alarm.presentation.components.AlarmDrawer
import app.grapheneos.deskclock.alarm.presentation.components.AlarmListItem
import app.grapheneos.deskclock.alarm.presentation.components.DialWithDialog
import app.grapheneos.deskclock.core.presentation.FloatingActionButton
import app.grapheneos.deskclock.core.presentation.Layout
import app.grapheneos.deskclock.core.presentation.components.lazyGroup
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar

@Composable
fun AlarmScreen(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlarmViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var editingAlarmId by remember { mutableStateOf<Long?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }

    val editingAlarm = remember(editingAlarmId, state.alarms) {
        state.alarms.find { it.alarm.id == editingAlarmId }
    }

    AlarmContent(
        state = state,
        modifier = modifier,
        onAction = viewModel::handleAction,
        onNavigateToSettings = onNavigateToSettings,
        onAddAlarmClick = { showTimePicker = true },
        onAlarmClick = { alarmWithInstance -> editingAlarmId = alarmWithInstance.alarm.id }
    )

    if (showTimePicker) {
        val now = Calendar.getInstance()
        DialWithDialog(
            initialHour = now.get(Calendar.HOUR_OF_DAY),
            initialMinute = now.get(Calendar.MINUTE),
            onConfirm = { pickerState ->
                viewModel.handleAction(
                    AlarmAction.AddAlarm(
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

    editingAlarm?.let { alarmWithInstance ->
        AlarmDrawer(
            alarmWithInstance = alarmWithInstance,
            ringtones = state.ringtones,
            onDismissRequest = {
                viewModel.handleAction(AlarmAction.StopPreview)
                editingAlarmId = null
            },
            onIntent = viewModel::handleAction,
            onDelete = {
                viewModel.handleAction(AlarmAction.DeleteAlarm(alarmWithInstance.alarm))
                editingAlarmId = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmContent(
    state: AlarmState,
    modifier: Modifier = Modifier,
    onAction: (AlarmAction) -> Unit,
    onNavigateToSettings: () -> Unit,
    onAddAlarmClick: () -> Unit,
    onAlarmClick: (AlarmWithInstance) -> Unit,
) {
    val view = LocalView.current
    val listState = rememberLazyListState()

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tab_alarm)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier,
                text = {
                    Text(
                        text = stringResource(R.string.add_alarm)
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_alarm)
                    )
                },
                onClick = onAddAlarmClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (state.alarms.isEmpty()) {
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
                    contentPadding = Layout.contentPadding(innerPadding)
                ) {
                    lazyGroup(
                        items = state.alarms,
                        key = { it.alarm.id },
                        onClick = { onAlarmClick(it) }
                    ) { index, alarmWithInstance ->
                        AlarmListItem(
                            alarmWithInstance = alarmWithInstance,
                            index = index,
                            listSize = state.alarms.size,
                            onToggle = {
                                onAction(AlarmAction.ToggleAlarm(alarmWithInstance.alarm))
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            }
                        ) {
                            onAlarmClick(alarmWithInstance)
                            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        }
                    }
                }
            }
        }
    }
}
