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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.alarm.data.AlarmEntity
import app.grapheneos.deskclock.alarm.data.AlarmWithInstance
import app.grapheneos.deskclock.alarm.presentation.components.AlarmDrawer
import app.grapheneos.deskclock.alarm.presentation.components.AlarmListItem
import app.grapheneos.deskclock.alarm.presentation.components.DialWithDialog
import app.grapheneos.deskclock.core.navigation.LocalNavBackStack
import app.grapheneos.deskclock.core.navigation.Route
import app.grapheneos.deskclock.core.presentation.FloatingActionButton
import app.grapheneos.deskclock.core.presentation.Layout
import app.grapheneos.deskclock.core.presentation.components.groupitems.lazyGroup
import app.grapheneos.deskclock.core.theme.DeskClockTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar

@Composable
fun AlarmScreen(
    modifier: Modifier = Modifier,
    viewModel: AlarmViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backStack = LocalNavBackStack.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val alarmDeletedText = stringResource(R.string.alarm_deleted)
    val undoText = stringResource(R.string.undo)

    var editingAlarmId by remember { mutableStateOf<Long?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }

    val editingAlarm = remember(editingAlarmId, uiState.alarms) {
        uiState.alarms.find { it.alarm.id == editingAlarmId }
    }

    AlarmContent(
        uiState = uiState,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::handleIntent,
        onNavigateToSettings = { backStack.add(Route.Settings) },
        onAddAlarmClick = { showTimePicker = true },
        onAlarmClick = { alarmWithInstance -> editingAlarmId = alarmWithInstance.alarm.id }
    )

    if (showTimePicker) {
        val now = Calendar.getInstance()
        DialWithDialog(
            initialHour = now.get(Calendar.HOUR_OF_DAY),
            initialMinute = now.get(Calendar.MINUTE),
            onConfirm = { pickerState ->
                viewModel.handleIntent(
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

    editingAlarm?.let { alarmWithInstance ->
        AlarmDrawer(
            alarmWithInstance = alarmWithInstance,
            ringtones = uiState.ringtones,
            onDismissRequest = {
                viewModel.handleIntent(AlarmIntent.StopPreview)
                editingAlarmId = null
            },
            onIntent = viewModel::handleIntent,
            onDelete = {
                viewModel.handleIntent(AlarmIntent.DeleteAlarm(alarmWithInstance.alarm))
                editingAlarmId = null

                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = alarmDeletedText,
                        actionLabel = undoText,
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.handleIntent(AlarmIntent.RestoreAlarm(alarmWithInstance.alarm))
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmContent(
    uiState: AlarmUiState,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onIntent: (AlarmIntent) -> Unit,
    onNavigateToSettings: () -> Unit,
    onAddAlarmClick: () -> Unit,
    onAlarmClick: (AlarmWithInstance) -> Unit,
) {
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
        snackbarHost = {
            SnackbarHost(snackbarHostState)
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
                        key = { it.alarm.id },
                        onClick = { onAlarmClick(it) }
                    ) { alarmWithInstance ->
                        AlarmListItem(
                            alarmWithInstance = alarmWithInstance,
                            onToggle = {
                                onIntent(AlarmIntent.ToggleAlarm(alarmWithInstance.alarm))
                            }
                        ) {
                            onAlarmClick(alarmWithInstance)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AlarmScreenPreview() {
    DeskClockTheme {
        AlarmContent(
            uiState = AlarmUiState(
                alarms = listOf(
                    AlarmWithInstance(
                        alarm = AlarmEntity(
                            id = 1,
                            hour = 7,
                            minute = 30,
                            daysOfWeek = 31, // Mon-Fri
                            isEnabled = true,
                            label = "Wake up"
                        ),
                        instance = null
                    ),
                    AlarmWithInstance(
                        alarm = AlarmEntity(
                            id = 2,
                            hour = 9,
                            minute = 0,
                            daysOfWeek = 64, // Sat
                            isEnabled = false,
                            label = "Gym"
                        ),
                        instance = null
                    )
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onIntent = {},
            onNavigateToSettings = {},
            onAddAlarmClick = {},
            onAlarmClick = {}
        )
    }
}
