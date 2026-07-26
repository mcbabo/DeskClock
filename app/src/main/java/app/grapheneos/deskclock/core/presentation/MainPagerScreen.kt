package app.grapheneos.deskclock.core.presentation

import android.view.HapticFeedbackConstants
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import app.grapheneos.deskclock.alarm.presentation.AlarmScreen
import app.grapheneos.deskclock.clock.presentation.ClockScreen
import app.grapheneos.deskclock.core.navigation.ClockTab
import app.grapheneos.deskclock.stopwatch.presentation.StopwatchScreen
import app.grapheneos.deskclock.timer.presentation.TimerScreen
import kotlinx.coroutines.launch

@Composable
fun MainPagerScreen(
    onNavigateToSettings: () -> Unit,
) {
    val tabs = ClockTab.entries
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val view = LocalView.current

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
                ClockTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = stringResource(tab.titleRes),
                            )
                        },
                        label = { Text(stringResource(tab.titleRes)) },
                        selected = pagerState.currentPage == index,
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
            when (tabs[pageIndex]) {
                ClockTab.Alarm -> {
                    AlarmScreen(onNavigateToSettings = onNavigateToSettings)
                }

                ClockTab.WorldClock -> {
                    ClockScreen(onNavigateToSettings = onNavigateToSettings)
                }

                ClockTab.Timer -> {
                    TimerScreen(onNavigateToSettings = onNavigateToSettings)
                }

                ClockTab.Stopwatch -> {
                    StopwatchScreen(onNavigateToSettings = onNavigateToSettings)
                }
            }
        }
    }
}
