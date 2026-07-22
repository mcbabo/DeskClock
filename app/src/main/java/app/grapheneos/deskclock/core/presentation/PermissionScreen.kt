package app.grapheneos.deskclock.core.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.BatteryAlert
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.core.presentation.components.groupitems.GroupRow
import app.grapheneos.deskclock.core.presentation.components.groupitems.ListGroup

@Composable
fun PermissionScreen(
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
        PermissionContent(
            permissions = permissions,
            onContinue = { isContinueClicked = true }
        )
    }
}

@Composable
fun PermissionContent(
    permissions: List<PermissionUiState>,
    onContinue: () -> Unit
) {
    val allGranted = permissions.all { it.isGranted }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
            title = "Permissions"
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
                                    text = "Configure",
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
    PermissionContent(
        listOf(
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
                isGranted = false,
                icon = Icons.Outlined.Layers,
                launchAction = { }
            )
        )
    ) {}
}

private fun buildPermissionList(
    context: Context,
    notificationLauncher: ActivityResultLauncher<String>,
    settingsLauncher: ActivityResultLauncher<Intent>
): List<PermissionUiState> {
    val list = mutableListOf<PermissionUiState>()

    val isGranted =
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    list.add(
        PermissionUiState(
            title = context.getString(R.string.notifications),
            description = context.getString(R.string.perms_notifications_desc),
            isGranted = isGranted,
            icon = Icons.Outlined.Notifications,
            launchAction = { notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
        )
    )

    val isOverlayGranted = Settings.canDrawOverlays(context)
    list.add(
        PermissionUiState(
            title = context.getString(R.string.perms_display_over_apps),
            description = context.getString(R.string.perms_display_over_apps_desc),
            isGranted = isOverlayGranted,
            icon = Icons.Outlined.Layers,
            launchAction = {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    "package:${context.packageName}".toUri()
                )
                settingsLauncher.launch(intent)
            }
        )
    )

    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val isBatteryIgnored = powerManager.isIgnoringBatteryOptimizations(context.packageName)
    list.add(
        PermissionUiState(
            title = context.getString(R.string.perms_battery),
            description = context.getString(R.string.perms_battery_desc),
            isGranted = isBatteryIgnored,
            icon = Icons.Outlined.BatteryAlert,
            launchAction = {
                val intent = Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    "package:${context.packageName}".toUri()
                )
                settingsLauncher.launch(intent)
            }
        )
    )

    return list
}
