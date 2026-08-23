package app.grapheneos.deskclock.core.audio

import android.animation.ValueAnimator
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.UserManager
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import androidx.core.net.toUri
import app.grapheneos.deskclock.core.util.Constants
import app.grapheneos.deskclock.core.util.UriScheme
import app.grapheneos.deskclock.core.util.uriScheme

/**
 * Handles audio playback for alerts (Alarms, Timers).
 * Supports looping, custom volumes, and gradual volume increase.
 */
class AudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var focusRequest: AudioFocusRequest? = null
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private var volumeAnimator: ValueAnimator? = null

    private val defaultUri: Uri
        get() = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: "${UriScheme.RESOURCE.value}://${context.packageName}/raw/neptunium".toUri()

    fun playAlarm(
        uri: Uri?,
        loop: Boolean = true,
        alarm: Boolean = true,
        volume: Float? = null,
        graduallyIncreaseVolume: Boolean = false,
        graduallyIncreaseVolumeDuration: Int = 30,
        fallbackUri: Uri? = null
    ) {
        stop()

        val attributes = buildAudioAttributes(alarm)
        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(attributes)
            .build()

        audioManager.requestAudioFocus(focusRequest!!)

        val ringtoneUri = uri ?: defaultUri
        val userManager = context.getSystemService(UserManager::class.java)
        val isUserUnlocked = userManager?.isUserUnlocked ?: true

        val isSafeScheme =
            ringtoneUri.uriScheme == UriScheme.RESOURCE || ringtoneUri.uriScheme == UriScheme.FILE
        if (!isUserUnlocked && !isSafeScheme) {
            playFallback(
                attributes,
                loop,
                volume,
                graduallyIncreaseVolume,
                graduallyIncreaseVolumeDuration,
                fallbackUri
            )
            return
        }

        runCatching {
            startMediaPlayer(
                ringtoneUri,
                attributes,
                loop,
                volume,
                graduallyIncreaseVolume,
                graduallyIncreaseVolumeDuration
            )
        }.onFailure {
            Log.w(Constants.TAG_AUDIO_PLAYER, "Failed to play URI: $uri", it)
            playFallback(
                attributes,
                loop,
                volume,
                graduallyIncreaseVolume,
                graduallyIncreaseVolumeDuration,
                fallbackUri
            )
        }
    }

    private fun playFallback(
        attributes: AudioAttributes,
        loop: Boolean,
        volume: Float?,
        graduallyIncreaseVolume: Boolean,
        graduallyIncreaseVolumeDuration: Int,
        fallbackUri: Uri? = null
    ) {
        runCatching {
            val ringtoneUri = fallbackUri ?: defaultUri
            startMediaPlayer(
                ringtoneUri,
                attributes,
                loop,
                volume,
                graduallyIncreaseVolume,
                graduallyIncreaseVolumeDuration
            )
        }.onFailure {
            Log.e(Constants.TAG_AUDIO_PLAYER, "Critical: Fallback also failed", it)
        }
    }

    private fun startMediaPlayer(
        uri: Uri,
        attributes: AudioAttributes,
        loop: Boolean,
        volume: Float?,
        graduallyIncreaseVolume: Boolean,
        graduallyIncreaseVolumeDuration: Int
    ) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, uri)
            setAudioAttributes(attributes)
            isLooping = loop
            prepare()

            val targetVolume = volume ?: 1.0f
            if (graduallyIncreaseVolume) {
                setVolume(0f, 0f)
                volumeAnimator = ValueAnimator.ofFloat(0f, targetVolume).apply {
                    duration = graduallyIncreaseVolumeDuration * 1000L
                    addUpdateListener { animator ->
                        val currentVolume = animator.animatedValue as Float
                        runCatching { mediaPlayer?.setVolume(currentVolume, currentVolume) }
                            .onFailure { animator.cancel() }
                    }
                    start()
                }
            } else {
                setVolume(targetVolume, targetVolume)
            }
            start()
        }
    }

    private fun buildAudioAttributes(alarm: Boolean): AudioAttributes {
        val usage = if (alarm) {
            AudioAttributes.USAGE_ALARM
        } else {
            AudioAttributes.USAGE_MEDIA
        }

        return AudioAttributes.Builder()
            .setUsage(usage)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
    }

    fun stop() {
        try {
            volumeAnimator?.cancel()
            volumeAnimator = null
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.d(Constants.TAG_AUDIO_PLAYER, "Error stopping player: ${e.message}")
        } finally {
            mediaPlayer = null
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        }
    }
}

/**
 * Manages haptic feedback (vibration) for active alerts.
 */
class VibrationManager(context: Context) {
    private val vibratorManager =
        context.getSystemService(VibratorManager::class.java).defaultVibrator

    fun startAlarmVibration() {
        val effect = VibrationEffect.createWaveform(
            longArrayOf(0, Constants.Alarm.WAVE_FORM, Constants.Alarm.WAVE_FORM),
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
