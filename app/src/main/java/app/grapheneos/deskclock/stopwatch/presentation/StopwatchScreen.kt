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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchScreen(
    uiState: StopwatchUiState,
    elapsedMillisFlow: Flow<Long>,
    onIntent: (StopwatchIntent) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.tab_stopwatch)) },
                navigationIcon = {
                    IconButton(onClick = onSettingsClick) {
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
            val activeState = uiState as? StopwatchUiState.Active

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                StopwatchTimeText(
                    elapsedMillisFlow = elapsedMillisFlow,
                    initialMillis = activeState?.elapsedMillis ?: 0L,
                    isRunning = activeState?.isRunning ?: false,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFeatureSettings = "tnum"
                    )
                )
            }

            StopwatchControls(
                isRunning = activeState?.isRunning ?: false,
                canReset = activeState?.canReset ?: false,
                onStartPause = {
                    if (activeState?.isRunning == true) {
                        onIntent(StopwatchIntent.Pause)
                    } else {
                        onIntent(StopwatchIntent.StartOrResume)
                    }
                },
                onLapOrReset = {
                    if (activeState?.isRunning == true) {
                        onIntent(StopwatchIntent.Lap)
                    } else {
                        onIntent(StopwatchIntent.Reset)
                    }
                }
            )

            if (activeState != null && activeState.hasLaps) {
                val lapString = stringResource(R.string.lap)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(Layout.GroupedList.ItemSpacing),
                    contentPadding = Layout.contentPadding(extraBottom = Layout.ScreenVertical)
                ) {
                    lazyGroup(
                        items = activeState.laps,
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
        StopwatchScreen(
            uiState = StopwatchUiState.Active(
                isRunning = true,
                elapsedMillis = 65_320L,
                laps = listOf(
                    Lap(3, 10_000L, 65_320L),
                    Lap(2, 20_120L, 55_320L),
                    Lap(1, 35_200L, 35_200L)
                )
            ),
            elapsedMillisFlow = flowOf(65_320L),
            onIntent = {},
            onSettingsClick = {}
        )
    }
}
