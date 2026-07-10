package app.grapheneos.deskclock.core.theme

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import com.google.android.material.color.MaterialColors

private val darkScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val lightScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

fun Color.applyOpacity(enabled: Boolean): Color = if (enabled) this else this.copy(alpha = 0.62f)

@Composable
@ReadOnlyComposable
fun Color.harmonizeWith(other: Color) =
    Color(MaterialColors.harmonize(this.toArgb(), other.toArgb()))

@Composable
@ReadOnlyComposable
fun Color.harmonizeWithPrimary(): Color =
    this.harmonizeWith(other = MaterialTheme.colorScheme.primary)

@Composable
fun DeskClockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val targetColorScheme =
        remember(darkTheme, dynamicColor) {
            when {
                dynamicColor -> {
                    if (darkTheme) {
                        dynamicDarkColorScheme(context)
                    } else {
                        dynamicLightColorScheme(context)
                    }
                }

                darkTheme -> darkScheme
                else -> lightScheme
            }
        }

    Crossfade(targetState = targetColorScheme, label = "ThemeTransition") { colorScheme ->
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun SystemBarsTheme(backgroundColor: Color = MaterialTheme.colorScheme.background) {
    val activity = LocalActivity.current
    val insetsController =
        WindowCompat.getInsetsController(activity?.window!!, activity.window.decorView)
    val isLightBackground = backgroundColor.luminance() > 0.5f

    LaunchedEffect(backgroundColor) {
        activity.window?.setBackgroundDrawable(
            backgroundColor.toArgb().toDrawable()
        )

        activity.window.setNavigationBarContrastEnforced(false)

        insetsController.isAppearanceLightStatusBars = isLightBackground
        insetsController.isAppearanceLightNavigationBars = isLightBackground
    }
}
