package app.grapheneos.deskclock.alarm.presentation.popup.styles

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.alarm.presentation.AlarmUiModel
import app.grapheneos.deskclock.core.presentation.Layout
import app.grapheneos.deskclock.core.presentation.screenPadding
import app.grapheneos.deskclock.core.util.formatSystemTime
import kotlinx.coroutines.delay
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds

@Composable
fun AlarmPopUpBase(
    alarm: AlarmUiModel?,
    labelText: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(ZonedDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1.seconds)
            currentTime = ZonedDateTime.now()
        }
    }

    val currentTimeText = formatSystemTime(
        context,
        currentTime.hour,
        currentTime.minute
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .screenPadding()
                .padding(vertical = Layout.ScreenVertical),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = currentTimeText,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = currentTime.run {
                        val locale = LocalLocale.current.platformLocale
                        format(DateTimeFormatter.ofPattern("EEEE, MMMM d", locale))
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Text(
                    text = labelText,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (alarm != null) {
                    Text(
                        text = stringResource(R.string.alarm_n, alarm.timeText),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            content()
        }
    }
}
