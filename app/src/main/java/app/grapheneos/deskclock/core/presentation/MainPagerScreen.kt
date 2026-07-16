package app.grapheneos.deskclock.core.presentation

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.grapheneos.deskclock.alarm.presentation.AlarmScreen
import app.grapheneos.deskclock.clock.presentation.ClockScreen
import app.grapheneos.deskclock.core.navigation.ClockTab
import kotlinx.coroutines.launch

@Composable
fun MainPagerScreen(
    onNavigateToSettings: () -> Unit,
) {
    val tabs = ClockTab.entries
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

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

                ClockTab.Timer -> {}
                ClockTab.Stopwatch -> {}
            }
        }
    }
}
