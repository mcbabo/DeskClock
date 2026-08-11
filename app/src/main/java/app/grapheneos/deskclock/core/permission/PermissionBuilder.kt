package app.grapheneos.deskclock.core.permission

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Notifications
import androidx.core.net.toUri
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.core.presentation.PermissionUiState
import app.grapheneos.deskclock.core.util.Constants

@SuppressLint("BatteryLife")
fun buildPermissionList(
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
                    "${Constants.SCHEME_PACKAGE}${context.packageName}".toUri()
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
                    "${Constants.SCHEME_PACKAGE}${context.packageName}".toUri()
                )
                settingsLauncher.launch(intent)
            }
        )
    )

    return list
}
