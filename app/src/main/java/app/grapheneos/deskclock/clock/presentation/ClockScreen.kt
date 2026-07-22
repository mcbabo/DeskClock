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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.clock.presentation.components.ClockListItem
import app.grapheneos.deskclock.clock.presentation.components.ClockSearch
import app.grapheneos.deskclock.clock.presentation.components.SearchBarInput
import app.grapheneos.deskclock.core.presentation.FloatingActionButton
import app.grapheneos.deskclock.core.presentation.Layout
import app.grapheneos.deskclock.core.presentation.components.groupitems.GroupItem
import app.grapheneos.deskclock.core.presentation.screenPadding
import app.grapheneos.deskclock.core.theme.DeskClockTheme
import org.koin.androidx.compose.koinViewModel
import java.time.ZoneId

@Composable
fun ClockScreen(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClockViewModel = koinViewModel()
) {
    val timeUiState by viewModel.timeUiState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ClockContent(
        modifier = modifier,
        uiState = uiState,
        timeUiState = timeUiState,
        onIntent = viewModel::handleIntent,
        onNavigateToSettings = onNavigateToSettings
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockContent(
    modifier: Modifier,
    uiState: ClockUiState,
    timeUiState: TimeUiState,
    onIntent: (ClockAction) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val searchListState = rememberLazyListState()
    val listState = rememberLazyListState()

    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(textFieldState.text) {
        snapshotFlow { textFieldState.text.toString() }.collect { text ->
            onIntent(ClockAction.UpdateSearchQuery(text))
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
            onIntent(ClockAction.ToggleSearch(false))
        }
    }

    val inputField = @Composable {
        SearchBarInput(
            searchBarState = searchBarState,
            textFieldState = textFieldState,
            onBack = {
                onIntent(ClockAction.ToggleSearch(false))
            }
        )
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.tab_clock))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                },
                actions = {
                    AnimatedVisibility(
                        visible = uiState.zoneClocks.isNotEmpty()
                    ) {
                        IconButton(
                            onClick = { isEditing = !isEditing }
                        ) {
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
                onIntent(ClockAction.AddTimeZone(it))
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier,
                text = { Text(text = stringResource(R.string.add_clock)) },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.add_alarm)
                    )
                },
                onClick = { onIntent(ClockAction.ToggleSearch(true)) },
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
                    style = MaterialTheme.typography.displayLarge,
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
                                onClick = { onIntent(ClockAction.RemoveTimeZone(clock.zoneId)) },
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
fun ClockContentPreview() {
    DeskClockTheme {
        ClockContent(
            modifier = Modifier,
            timeUiState = TimeUiState(
                localTime = "12:00:00",
                localDate = "Mon, 1 Jan",
            ),
            uiState = ClockUiState(
                zoneClocks = listOf(
                    ClockUiModel(
                        ZoneId.of("Europe/Vienna"),
                        cityName = "Vienna",
                        hours = 12,
                        minutes = 0,
                        dayResId = R.string.today,
                        hoursDiff = 4L
                    )
                )
            ),
            onNavigateToSettings = {},
            onIntent = {}
        )
    }
}
