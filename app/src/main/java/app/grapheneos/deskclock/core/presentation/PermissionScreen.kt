@file:Suppress("MatchingDeclarationName")

package app.grapheneos.deskclock.core.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.core.permission.buildPermissionList
import app.grapheneos.deskclock.core.presentation.components.groupitems.GroupRow
import app.grapheneos.deskclock.core.presentation.components.groupitems.ListGroup
import app.grapheneos.deskclock.core.theme.DeskClockTheme

/**
 * UI state for a specific permission requirement.
 */
data class PermissionUiState(
    val title: String,
    val description: String,
    val isGranted: Boolean,
    val icon: ImageVector,
    val launchAction: () -> Unit
)

@Composable
fun PermissionGate(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var refreshTrigger by remember { mutableIntStateOf(0) }
    var isContinueClicked by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> refreshTrigger++ }

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ -> refreshTrigger++ }

    val initiallyAllGranted = remember {
        val initialList = buildPermissionList(context, notificationLauncher, settingsLauncher)
        initialList.all { it.isGranted }
    }

    val permissions = remember(refreshTrigger) {
        buildPermissionList(context, notificationLauncher, settingsLauncher)
    }

    if (initiallyAllGranted || isContinueClicked) {
        content()
    } else {
        PermissionScreen(
            permissions = permissions,
            onContinue = { isContinueClicked = true }
        )
    }
}

@Composable
fun PermissionScreen(
    permissions: List<PermissionUiState>,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val allGranted = permissions.all { it.isGranted }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .screenPadding(),
                contentAlignment = Alignment.BottomEnd
            ) {
                Button(
                    onClick = onContinue,
                    enabled = allGranted
                ) {
                    Text(text = stringResource(R.string.continue_value))
                }
            }
        }
    ) { innerPadding ->
        ListGroup(
            modifier = Modifier
                .padding(innerPadding)
                .screenPadding(),
            title = stringResource(R.string.permissions)
        ) {
            permissions.forEach { permission ->
                item(
                    onClick = if (!permission.isGranted) {
                        permission.launchAction
                    } else {
                        null
                    },
                ) {
                    GroupRow(
                        leadingContent = {
                            Icon(
                                imageVector = permission.icon,
                                contentDescription = permission.icon.toString(),
                                tint = if (permission.isGranted) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                        },
                        trailingContent = {
                            if (permission.isGranted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = stringResource(R.string.granted),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.configure),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        supportingContent = {
                            Text(
                                text = permission.description,
                                color = if (permission.isGranted) {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = permission.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (permission.isGranted) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PermissionScreenPreview() {
    DeskClockTheme {
        PermissionScreen(
            permissions = listOf(
                PermissionUiState(
                    title = "Notifications",
                    description = "Required to display active timers and persistent alarm alerts.",
                    isGranted = false,
                    icon = Icons.Outlined.Notifications,
                    launchAction = { }
                ),
                PermissionUiState(
                    title = "Display Over Other Apps",
                    description = "Allows alarms to wake the screen and display full-screen alerts natively.",
                    isGranted = true,
                    icon = Icons.Outlined.Layers,
                    launchAction = { }
                )
            ),
            onContinue = {}
        )
    }
}
