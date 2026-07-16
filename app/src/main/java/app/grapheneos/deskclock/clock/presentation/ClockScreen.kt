package app.grapheneos.deskclock.clock.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.clock.presentation.components.ClockListItem
import app.grapheneos.deskclock.core.presentation.FloatingActionButton
import app.grapheneos.deskclock.core.presentation.components.GroupItem
import app.grapheneos.deskclock.core.presentation.components.GroupRow
import app.grapheneos.deskclock.core.presentation.components.ListGroup
import app.grapheneos.deskclock.core.theme.DeskClockTheme
import org.koin.androidx.compose.koinViewModel
import java.time.ZoneId

@Composable
fun ClockScreen(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClockViewModel = koinViewModel()
) {
    val timeState by viewModel.timeState.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()

    ClockContent(
        modifier = modifier,
        state = state,
        timeState = timeState,
        onNavigateToSettings = onNavigateToSettings,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockContent(
    modifier: Modifier,
    state: ClockScreenState,
    timeState: TimeState,
    onNavigateToSettings: () -> Unit,
    onAction: (ClockAction) -> Unit
) {
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val searchListState = rememberLazyListState()
    val listState = rememberLazyListState()

    val expandedFab by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 10 }
    }

    val isFabVisible by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            !state.isSearchActive && totalItemsNumber > 0 && lastVisibleItemIndex < totalItemsNumber - 1
        }
    }

    LaunchedEffect(textFieldState.text) {
        snapshotFlow { textFieldState.text.toString() }.collect { text ->
            onAction(ClockAction.UpdateSearchQuery(text))
        }
    }

    LaunchedEffect(state.isSearchActive) {
        if (state.isSearchActive) {
            searchBarState.animateToExpanded()
        } else {
            searchBarState.animateToCollapsed()
            textFieldState.clearText()
        }
    }

    LaunchedEffect(searchBarState.currentValue) {
        if (searchBarState.currentValue == SearchBarValue.Collapsed) {
            onAction(ClockAction.ToggleSearch(false))
        }
    }

    val inputField = @Composable {
        SearchBarInput(
            searchBarState = searchBarState,
            textFieldState = textFieldState,
            onBack = {
                onAction(ClockAction.ToggleSearch(false))
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
                }
            )
            ExpandedFullScreenSearchBar(
                state = searchBarState,
                inputField = inputField
            ) {
                LazyColumn(
                    state = searchListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    state.filteredZones.forEach { (char, zones) ->
                        item(key = char) {
                            ListGroup(title = char.toString()) {
                                zones.forEachIndexed { index, zone ->
                                    GroupItem(
                                        index = index,
                                        count = zones.size,
                                        onClick = { onAction(ClockAction.AddTimeZone(zone)) }
                                    ) {
                                        GroupRow(
                                            content = {
                                                Text(zone.id.substringAfter('/').replace('_', ' '))
                                            },
                                            supportingContent = {
                                                Text(
                                                    text = zone.id.substringBefore('/'),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier,
                text = { Text(text = stringResource(R.string.add_clock)) },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add_alarm)
                    )
                },
                isExpanded = expandedFab,
                isVisible = isFabVisible,
                onClick = { onAction(ClockAction.ToggleSearch(true)) },
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = timeState.localTime,
                    style = MaterialTheme.typography.displayLarge,
                )
                Text(text = timeState.localDate, style = MaterialTheme.typography.bodyLarge)
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                itemsIndexed(state.zoneClocks) { index, clock ->
                    ClockListItem(
                        display = clock,
                        index = index,
                        listSize = state.zoneClocks.size,
                        onDelete = { onAction(ClockAction.RemoveTimeZone(clock.zoneId)) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarInput(
    searchBarState: SearchBarState,
    textFieldState: TextFieldState,
    onBack: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    SearchBarDefaults.InputField(
        modifier = Modifier,
        searchBarState = searchBarState,
        textFieldState = textFieldState,
        onSearch = { keyboardController?.hide() },
        placeholder = { Text(text = stringResource(R.string.search)) },
        leadingIcon = {
            if (searchBarState.currentValue == SearchBarValue.Expanded) {
                IconButton(
                    onClick = onBack
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                    )
                }
            } else {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = stringResource(R.string.search),
                )
            }
        }
    )
}

@Preview
@Composable
fun ClockContentPreview() {
    DeskClockTheme {
        ClockContent(
            modifier = Modifier,
            timeState = TimeState(
                localTime = "12:00:00",
                localDate = "Mon, 1 Jan",
            ),
            state = ClockScreenState(
                zoneClocks = listOf(
                    ClockUiModel(
                        ZoneId.of("Europe/Vienna"),
                        cityName = "Vienna",
                        timeText = "12:00",
                        dayResId = R.string.today,
                        hoursDiff = 4L
                    )
                )
            ),
            onNavigateToSettings = {},
            onAction = {}
        )
    }
}
