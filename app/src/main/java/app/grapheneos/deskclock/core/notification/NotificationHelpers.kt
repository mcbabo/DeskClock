@file:Suppress("MatchingDeclarationName")

package app.grapheneos.deskclock.core.notification

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import app.grapheneos.deskclock.R

/**
 * Creates a [NotificationCompat.Builder] with common settings for alerts.
 */
fun Context.baseNotificationBuilder(
    channelId: String,
    @DrawableRes icon: Int = R.drawable.ic_alarm
): NotificationCompat.Builder {
    return NotificationCompat.Builder(this, channelId)
        .setSmallIcon(icon)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
}
