package app.grapheneos.deskclock.clock.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.clock.presentation.components.ClockListItem
import app.grapheneos.deskclock.clock.presentation.components.ClockSearch
import app.grapheneos.deskclock.clock.presentation.components.SearchBarInput
import app.grapheneos.deskclock.core.presentation.FloatingActionButton
import app.grapheneos.deskclock.core.presentation.Layout
import app.grapheneos.deskclock.core.presentation.components.groupitems.GroupItem
import app.grapheneos.deskclock.core.presentation.screenPadding
import app.grapheneos.deskclock.core.theme.DeskClockTheme
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockScreen(
    uiState: ClockUiState,
    timeUiState: TimeUiState,
    onIntent: (ClockIntent) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    triggerAdd: Boolean = false,
    onAddTriggered: () -> Unit = {}
) {
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val searchListState = rememberLazyListState()
    val listState = rememberLazyListState()

    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(triggerAdd) {
        if (triggerAdd) {
            onIntent(ClockIntent.ToggleSearch(true))
            onAddTriggered()
        }
    }
    LaunchedEffect(textFieldState.text) {
        snapshotFlow { textFieldState.text.toString() }.collect { text ->
            onIntent(ClockIntent.UpdateSearchQuery(text))
        }
    }

    LaunchedEffect(uiState.isSearchActive) {
        if (uiState.isSearchActive) {
            searchBarState.animateToExpanded()
        } else {
            searchBarState.animateToCollapsed()
            textFieldState.clearText()
        }
    }

    LaunchedEffect(searchBarState.currentValue) {
        if (searchBarState.currentValue == SearchBarValue.Collapsed) {
            onIntent(ClockIntent.ToggleSearch(false))
        }
    }

    val inputField = @Composable {
        SearchBarInput(
            searchBarState = searchBarState,
            textFieldState = textFieldState,
            onBack = { onIntent(ClockIntent.ToggleSearch(false)) }
        )
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.tab_clock)) },
                navigationIcon = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                },
                actions = {
                    AnimatedVisibility(visible = uiState.zoneClocks.isNotEmpty()) {
                        IconButton(onClick = { isEditing = !isEditing }) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null
                            )
                        }
                    }
                }
            )
            ClockSearch(
                uiState.filteredZones,
                searchListState,
                searchBarState,
                inputField
            ) {
                onIntent(ClockIntent.AddTimeZone(it))
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                text = { Text(text = stringResource(R.string.add_clock)) },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.add_alarm)
                    )
                },
                onClick = { onIntent(ClockIntent.ToggleSearch(true)) },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .screenPadding(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = timeUiState.localTime,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFeatureSettings = "tnum"
                    ),
                )
                Text(
                    text = timeUiState.localDate,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = Layout.ScreenHorizontal))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(Layout.GroupedList.ItemSpacing),
                contentPadding = Layout.contentPadding()
            ) {
                itemsIndexed(
                    items = uiState.zoneClocks,
                    key = { _, clock -> clock.zoneId }
                ) { index, clock ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        AnimatedVisibility(
                            visible = isEditing,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally()
                        ) {
                            IconButton(
                                onClick = { onIntent(ClockIntent.RemoveTimeZone(clock.zoneId)) },
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = stringResource(R.string.delete),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        GroupItem(
                            index = index,
                            count = uiState.zoneClocks.size,
                            modifier = Modifier.weight(1f)
                        ) {
                            ClockListItem(display = clock)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ClockScreenPreview() {
    DeskClockTheme {
        ClockScreen(
            timeUiState = TimeUiState(
                localTime = "12:45:00",
                localDate = "Wed, 29. Jul",
            ),
            uiState = ClockUiState(
                zoneClocks = listOf(
                    ClockUiModel(
                        ZoneId.of("Europe/Vienna"),
                        cityName = "Vienna",
                        hours = 12,
                        minutes = 45,
                        dayResId = R.string.today,
                        hoursDiff = 0L
                    ),
                    ClockUiModel(
                        ZoneId.of("America/New_York"),
                        cityName = "New York",
                        hours = 6,
                        minutes = 45,
                        dayResId = R.string.today,
                        hoursDiff = -6L
                    )
                )
            ),
            onIntent = {},
            onSettingsClick = {}
        )
    }
}
