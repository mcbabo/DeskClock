package app.grapheneos.deskclock.stopwatch.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.core.presentation.Layout
import app.grapheneos.deskclock.core.presentation.components.groupitems.lazyGroup
import app.grapheneos.deskclock.core.theme.DeskClockTheme
import app.grapheneos.deskclock.stopwatch.data.Lap
import app.grapheneos.deskclock.stopwatch.presentation.components.LapListItem
import app.grapheneos.deskclock.stopwatch.presentation.components.StopwatchControls
import app.grapheneos.deskclock.stopwatch.presentation.components.StopwatchTimeText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.androidx.compose.koinViewModel

@Composable
fun StopwatchScreen(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StopwatchViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    StopwatchContent(
        modifier = modifier,
        uiState = state,
        elapsedMillisFlow = viewModel.elapsedMillis,
        onIntent = viewModel::handleIntent,
        onNavigateToSettings = onNavigateToSettings
    )
}

@Composable
fun StopwatchContent(
    modifier: Modifier = Modifier,
    uiState: StopwatchUiState,
    elapsedMillisFlow: Flow<Long>,
    onIntent: (StopwatchIntent) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.tab_stopwatch))
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Layout.SectionSpacing)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                StopwatchTimeText(
                    elapsedMillisFlow = elapsedMillisFlow,
                    initialMillis = uiState.elapsedMillis,
                    isRunning = uiState.isRunning,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFeatureSettings = "tnum"
                    )
                )
            }

            StopwatchControls(
                isRunning = uiState.isRunning,
                canReset = uiState.canReset,
                onStartPause = {
                    if (uiState.isRunning) {
                        onIntent(StopwatchIntent.Pause)
                    } else {
                        onIntent(StopwatchIntent.StartOrResume)
                    }
                },
                onLapOrReset = {
                    if (uiState.isRunning) {
                        onIntent(StopwatchIntent.Lap)
                    } else {
                        onIntent(StopwatchIntent.Reset)
                    }
                }
            )

            if (uiState.hasLaps) {
                val lapString = stringResource(R.string.lap)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(Layout.GroupedList.ItemSpacing),
                    contentPadding = Layout.contentPadding(extraBottom = Layout.ScreenVertical)
                ) {
                    lazyGroup(
                        items = uiState.laps,
                        title = lapString,
                        key = { it.number }
                    ) { lap ->
                        LapListItem(lap = lap)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun StopwatchScreenPreview() {
    DeskClockTheme {
        StopwatchContent(
            uiState = StopwatchUiState(
                isRunning = true,
                elapsedMillis = 65_320L,
                laps = listOf(
                    Lap(2, 30_120L, 65_320L),
                    Lap(1, 35_200L, 35_200L)
                )
            ),
            elapsedMillisFlow = flowOf(65_320L),
            onIntent = {},
            onNavigateToSettings = {}
        )
    }
}
