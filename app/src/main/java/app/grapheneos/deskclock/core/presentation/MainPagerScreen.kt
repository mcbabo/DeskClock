package app.grapheneos.deskclock.core.presentation

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.grapheneos.deskclock.alarm.presentation.AlarmScreen
import app.grapheneos.deskclock.alarm.presentation.AlarmViewModel
import app.grapheneos.deskclock.clock.presentation.ClockScreen
import app.grapheneos.deskclock.clock.presentation.ClockViewModel
import app.grapheneos.deskclock.core.navigation.ClockTab
import app.grapheneos.deskclock.core.navigation.LocalNavBackStack
import app.grapheneos.deskclock.core.navigation.Route
import app.grapheneos.deskclock.stopwatch.presentation.StopwatchScreen
import app.grapheneos.deskclock.stopwatch.presentation.StopwatchViewModel
import app.grapheneos.deskclock.timer.presentation.TimerScreen
import app.grapheneos.deskclock.timer.presentation.TimerViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainPagerScreen(
    alarmViewModel: AlarmViewModel = koinViewModel(),
    clockViewModel: ClockViewModel = koinViewModel(),
    timerViewModel: TimerViewModel = koinViewModel(),
    stopwatchViewModel: StopwatchViewModel = koinViewModel()
) {
    val tabs = ClockTab.entries
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val backStack = LocalNavBackStack.current

    val alarmUiState by alarmViewModel.uiState.collectAsStateWithLifecycle()
    val clockUiState by clockViewModel.uiState.collectAsStateWithLifecycle()
    val timeUiState by clockViewModel.timeUiState.collectAsStateWithLifecycle()
    val timerUiState by timerViewModel.uiState.collectAsStateWithLifecycle()
    val stopwatchUiState by stopwatchViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(pagerState) {
        var isInitial = true
        snapshotFlow { pagerState.currentPage }.collect { _ ->
            if (!isInitial) {
                view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
            }
            isInitial = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    val selected = pagerState.currentPage == index
                    val scale = remember { Animatable(1f) }

                    LaunchedEffect(key1 = selected) {
                        if (!selected) return@LaunchedEffect
                        scale.snapTo(1f)
                        scale.animateTo(1.15f, tween(120))
                        scale.animateTo(1f, tween(120))
                    }

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.icon,
                                contentDescription = stringResource(tab.titleRes),
                                modifier = Modifier.scale(scale.value)
                            )
                        },
                        label = { Text(stringResource(tab.titleRes)) },
                        selected = selected,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        }
                    )
                }
            }
        }
    ) { outerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(outerPadding),
            beyondViewportPageCount = 1
        ) { pageIndex ->
            val onSettingsClick = {
                backStack.add(Route.Settings)
                Unit
            }
            when (tabs[pageIndex]) {
                ClockTab.Alarm -> {
                    AlarmScreen(
                        uiState = alarmUiState,
                        onIntent = alarmViewModel::handleIntent,
                        onSettingsClick = onSettingsClick
                    )
                }

                ClockTab.WorldClock -> {
                    ClockScreen(
                        uiState = clockUiState,
                        timeUiState = timeUiState,
                        onIntent = clockViewModel::handleIntent,
                        onSettingsClick = onSettingsClick
                    )
                }

                ClockTab.Timer -> {
                    TimerScreen(
                        uiState = timerUiState,
                        onIntent = timerViewModel::handleIntent,
                        onSettingsClick = onSettingsClick
                    )
                }

                ClockTab.Stopwatch -> {
                    StopwatchScreen(
                        uiState = stopwatchUiState,
                        elapsedMillisFlow = stopwatchViewModel.elapsedMillis,
                        onIntent = stopwatchViewModel::handleIntent,
                        onSettingsClick = onSettingsClick
                    )
                }
            }
        }
    }
}
