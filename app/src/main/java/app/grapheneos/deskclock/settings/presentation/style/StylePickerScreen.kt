package app.grapheneos.deskclock.settings.presentation.style

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.settings.data.PopUpStyle

const val PREVIEW_SCALE = 0.7f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StylePickerScreen(
    title: String,
    currentStyle: PopUpStyle,
    onStyleSelected: (PopUpStyle) -> Unit,
    onBack: () -> Unit,
    previewContent: @Composable (PopUpStyle) -> Unit
) {
    var selectedStyle by remember(currentStyle) { mutableStateOf(currentStyle) }
    val styles = PopUpStyle.entries
    val initialPage = styles.indexOf(currentStyle).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage) { styles.size }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { onStyleSelected(selectedStyle) },
                        modifier = Modifier.padding(end = 8.dp),
                        enabled = selectedStyle != currentStyle
                    ) {
                        Text(text = stringResource(R.string.set_style))
                    }
                }
            )
        }
    ) { innerPadding ->
        val view = LocalView.current
        val windowInfo = LocalWindowInfo.current
        val density = LocalDensity.current
        val containerSize = windowInfo.containerSize
        val screenWidth = with(density) { containerSize.width.toDp() }
        val screenHeight = with(density) { containerSize.height.toDp() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 48.dp),
                    pageSpacing = 16.dp,
                    verticalAlignment = Alignment.CenterVertically
                ) { page ->
                    val style = styles[page]
                    val isSelected = selectedStyle == style
                    val borderColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                    val borderWeight = if (isSelected) 4.dp else 2.dp

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            modifier = Modifier
                                .requiredSize(
                                    screenWidth * PREVIEW_SCALE,
                                    screenHeight * PREVIEW_SCALE
                                )
                                .clip(RoundedCornerShape(32.dp))
                                .border(
                                    width = borderWeight,
                                    color = borderColor,
                                    shape = RoundedCornerShape(32.dp)
                                )
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                    selectedStyle = style
                                },
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = if (isSelected) 8.dp else 2.dp
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .requiredSize(screenWidth, screenHeight)
                                        .scale(PREVIEW_SCALE)
                                        .clipToBounds()
                                        .align(Alignment.Center)
                                ) {
                                    previewContent(style)
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                        .size(32.dp)
                                        .background(
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            },
                                            shape = CircleShape
                                        )
                                        .border(
                                            width = 2.dp,
                                            color = if (isSelected) {
                                                Color.Transparent
                                            } else {
                                                MaterialTheme.colorScheme.outline
                                            },
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = stringResource(style.titleRes),
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }

                Row(
                    Modifier
                        .height(50.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(styles.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        }
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
