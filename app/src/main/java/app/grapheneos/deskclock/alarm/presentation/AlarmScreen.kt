package app.grapheneos.deskclock.alarm.presentation

import android.Manifest
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.alarm.data.AlarmWithInstance
import app.grapheneos.deskclock.alarm.presentation.components.AlarmDrawer
import app.grapheneos.deskclock.alarm.presentation.components.AlarmListItem
import app.grapheneos.deskclock.alarm.presentation.components.DialWithDialog
import app.grapheneos.deskclock.core.presentation.LocalBottomClearance
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

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    AlarmContent(
        state = state,
        onIntent = viewModel::handleIntent,
        onSettingsClick = onNavigateToSettings,
        onAddAlarmClick = { showTimePicker = true },
        onAlarmClick = { alarmWithInstance -> editingAlarmId = alarmWithInstance.alarm.id },
        modifier = modifier
    )

    if (showTimePicker) {
        val now = Calendar.getInstance()
        DialWithDialog(
            initialHour = now.get(Calendar.HOUR_OF_DAY),
            initialMinute = now.get(Calendar.MINUTE),
            onConfirm = { pickerState ->
                viewModel.handleIntent(
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
            key = alarmWithInstance.alarm.id,
            alarmWithInstance = alarmWithInstance,
            onDismissRequest = {
                editingAlarmId = null
            },
            onIntent = viewModel::handleIntent,
            onDelete = {
                viewModel.handleIntent(AlarmAction.DeleteAlarm(alarmWithInstance.alarm))
                editingAlarmId = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmContent(
    state: AlarmState,
    onIntent: (AlarmAction) -> Unit,
    onSettingsClick: () -> Unit,
    onAddAlarmClick: () -> Unit,
    onAlarmClick: (AlarmWithInstance) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val bottomPadding = LocalBottomClearance.current
    val listState = rememberLazyListState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tab_alarm)) },
                navigationIcon = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAddAlarmClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_alarm)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            state = listState,
            contentPadding = PaddingValues(bottom = bottomPadding),
        ) {
            if (state.alarms.isEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.no_alarms_yet),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            items(state.alarms, key = { it.alarm.id }) { alarmWithInstance ->
                AlarmListItem(
                    alarmWithInstance = alarmWithInstance,
                    onToggle = {
                        onIntent(AlarmAction.ToggleAlarm(alarmWithInstance.alarm))
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    },
                    onClick = {
                        onAlarmClick(alarmWithInstance)
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    }
                )
            }
        }
    }
}
