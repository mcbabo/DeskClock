package app.grapheneos.deskclock.core.util

import android.content.Context
import android.text.format.DateFormat
import java.util.Calendar

fun formatSystemTime(context: Context, hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }

    val formatter = DateFormat.getTimeFormat(context)
    return formatter.format(calendar.time)
}
