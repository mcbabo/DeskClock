package app.grapheneos.deskclock.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.exitUntilCollapsedScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.alarm.presentation.components.RingtonePickerDialog
import app.grapheneos.deskclock.core.presentation.components.GroupItem
import app.grapheneos.deskclock.core.presentation.components.GroupRow
import app.grapheneos.deskclock.core.presentation.components.ListGroup
import app.grapheneos.deskclock.core.presentation.screenPadding
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    SettingsContent(
        state = state,
        onIntent = { intent -> viewModel.handleIntent(intent) },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit
) {
    val scrollBehavior = exitUntilCollapsedScrollBehavior()
    var showSnoozeDialog by remember { mutableStateOf(false) }
    var showRingtoneDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .screenPadding()
        ) {
            ListGroup(
                title = stringResource(R.string.general)
            ) {
                GroupItem(
                    index = 0,
                    count = 2,
                    onClick = {}
                ) {
                    GroupRow(
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.ColorLens,
                                contentDescription = null
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = state.settings.dynamicColors,
                                onCheckedChange = {
                                    onIntent(SettingsIntent.SetDynamicColors(!state.settings.dynamicColors))
                                }
                            )
                        },
                        supportingContent = {
                            Text(
                                text = stringResource(R.string.settings_dynamic_colors_desc),
                            )
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.settings_dynamic_colors),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                GroupItem(
                    index = 1,
                    count = 2,
                    onClick = {}
                ) {
                    GroupRow(
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.Colorize,
                                contentDescription = null
                            )
                        },
                        trailingContent = {
                            Text(
                                text = state.settings.themeMode.displayName
                            )
                        },
                        supportingContent = {
                            Text(
                                text = stringResource(R.string.setting_theme_desc),
                            )
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.setting_theme),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            ListGroup(
                title = stringResource(R.string.settings_alarm_and_timer)
            ) {
                val items = 3

                GroupItem(
                    index = 0,
                    count = items,
                    onClick = {
                        showSnoozeDialog = true
                    }
                ) {
                    GroupRow(
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.Snooze,
                                contentDescription = null
                            )
                        },
                        trailingContent = {
                            Text(
                                text = "${state.settings.snoozeDurationMinutes} min"
                            )
                        },
                        supportingContent = {
                            Text(
                                text = stringResource(R.string.settings_snooze_duration_desc),
                            )
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.settings_snooze_duration),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                GroupItem(
                    index = 1,
                    count = items,
                    onClick = { showRingtoneDialog = true }
                ) {
                    GroupRow(
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.Snooze,
                                contentDescription = null
                            )
                        },
                        trailingContent = {
                            Text(
                                text = state.settings.defaultRingtone.name
                            )
                        },
                        supportingContent = {
                            Text(
                                text = stringResource(R.string.settings_alarm_sound_desc),
                            )
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.settings_alarm_sound),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                GroupItem(
                    index = 2,
                    count = items,
                    onClick = { showRingtoneDialog = true }
                ) {
                    GroupRow(
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.Vibration,
                                contentDescription = null
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = state.settings.vibrate,
                                onCheckedChange = {
                                    onIntent(SettingsIntent.SetDynamicColors(!state.settings.vibrate))
                                }
                            )
                        },
                        supportingContent = {
                            Text(
                                text = stringResource(R.string.settings_vibration_desc),
                            )
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.settings_vibration),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }

    if (showSnoozeDialog) {
        SnoozeDialog(
            state.settings.snoozeDurationMinutes,
            onConfirm = {
                onIntent(SettingsIntent.SetSnoozeTime(it))
                showSnoozeDialog = false
            }
        ) {
            showSnoozeDialog = false
        }
    }

    if (showRingtoneDialog) {
        RingtonePickerDialog(
            ringtones = state.ringtones,
            initialUri = state.settings.defaultRingtone.uri,
            {
                onIntent(SettingsIntent.PlayPreview(it))
            },
            {
                onIntent(SettingsIntent.StopPreview)
            },
            {
                onIntent(SettingsIntent.StopPreview)
                showRingtoneDialog = false
            },
            {
                onIntent(SettingsIntent.SetDefaultRingtone(it))
                showRingtoneDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnoozeDialog(
    snoozeMinutes: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val predefinedSnoozeIntervals = listOf(2, 5, 10, 15, 20, 30)

    var selectedInterval by remember { mutableIntStateOf(snoozeMinutes) }

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        8.dp,
                        Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Snooze,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Snooze Duration",
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                HorizontalDivider(modifier = Modifier.fillMaxWidth())

                predefinedSnoozeIntervals.forEach { interval ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp)
                            .selectable(
                                selected = (selectedInterval == interval),
                                onClick = {
                                    selectedInterval = interval
                                    onConfirm(selectedInterval)
                                },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedInterval == interval),
                            onClick = null
                        )
                        Text(
                            text = "$interval minutes",
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsContent(
        SettingsUiState(),
        {},
        {}
    )
}
