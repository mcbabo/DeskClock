package app.grapheneos.deskclock.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import app.grapheneos.deskclock.core.presentation.MainPagerScreen

@Composable
fun NavigationRoot(
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(Route.Main)

    NavDisplay(
        backStack = backStack,
        entryProvider = { key ->
            when (key) {
                is Route.Main -> {
                    NavEntry(key) {
                        MainPagerScreen()
                    }
                }

                is Route.Settings -> {
                    NavEntry(key) {
                    }
                }

                else -> error("Unknown Navkey: $key")
            }
        }
    )
}
