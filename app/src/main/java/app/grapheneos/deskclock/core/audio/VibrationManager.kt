package app.grapheneos.deskclock.core.audio

import android.content.Context
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.VibratorManager
import app.grapheneos.deskclock.alarm.util.AlarmConstants

class VibrationManager(private val context: Context) {
    private val vibratorManager =
        context.getSystemService(VibratorManager::class.java).defaultVibrator

    fun startAlarmVibration() {
        val effect = VibrationEffect.createWaveform(
            longArrayOf(
                0,
                AlarmConstants.WAVE_FORM,
                AlarmConstants.WAVE_FORM
            ),
            0
        )
        val attributes = VibrationAttributes.Builder()
            .setUsage(VibrationAttributes.USAGE_ALARM)
            .build()
        vibratorManager.vibrate(effect, attributes)
    }

    fun stop() {
        vibratorManager.cancel()
    }
}
