package app.grapheneos.deskclock.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import app.grapheneos.deskclock.core.presentation.MainPagerScreen
import app.grapheneos.deskclock.core.presentation.PermissionScreen
import app.grapheneos.deskclock.core.theme.motion.clockDefaultTransitions
import app.grapheneos.deskclock.core.theme.motion.clockPopTransitions
import app.grapheneos.deskclock.settings.presentation.SettingsScreen
import app.grapheneos.deskclock.settings.presentation.style.AlarmStylePickerScreen
import app.grapheneos.deskclock.settings.presentation.style.TimerStylePickerScreen

private const val METADATA_PERMISSIONS_REQUIRED = "permissions_required"

@Composable
fun NavigationRoot(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalNavBackStack provides backStack) {
        NavDisplay(
            backStack = backStack,
            modifier = modifier,
            onBack = { backStack.removeLastOrNull() },
            transitionSpec = { clockDefaultTransitions() },
            popTransitionSpec = { clockPopTransitions() },
            predictivePopTransitionSpec = { clockPopTransitions() },
            entryDecorators = listOf(
                NavEntryDecorator { entry ->
                    val permissionsRequired =
                        entry.metadata[METADATA_PERMISSIONS_REQUIRED] as? Boolean ?: false
                    if (permissionsRequired) {
                        PermissionScreen { entry.Content() }
                    } else {
                        entry.Content()
                    }
                }
            ),
            entryProvider = entryProvider {
                entry<Route.Main>(
                    metadata = mapOf(METADATA_PERMISSIONS_REQUIRED to true)
                ) {
                    MainPagerScreen()
                }

                entry<Route.Settings> {
                    SettingsScreen(onBack = { backStack.removeLastOrNull() })
                }

                entry<Route.AlarmStylePicker> {
                    AlarmStylePickerScreen(onBack = { backStack.removeLastOrNull() })
                }

                entry<Route.TimerStylePicker> {
                    TimerStylePickerScreen(onBack = { backStack.removeLastOrNull() })
                }
            }
        )
    }
}
