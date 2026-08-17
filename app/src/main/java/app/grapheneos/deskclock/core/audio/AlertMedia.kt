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
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.core.util.Constants

/**
 * Handles audio playback for alerts (Alarms, Timers).
 * Supports looping, custom volumes, and gradual volume increase.
 */
class AudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var focusRequest: AudioFocusRequest? = null
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private var volumeAnimator: ValueAnimator? = null

    fun playAlarm(
        uriString: String?,
        loop: Boolean = true,
        alarm: Boolean = true,
        ringtoneVolume: Float? = null,
        graduallyIncreaseVolume: Boolean = false,
        graduallyIncreaseVolumeDuration: Int = 30,
        fallbackUriString: String? = null
    ) {
        stop()

        val attributes = buildAudioAttributes(alarm, ringtoneVolume)
        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(attributes)
            .build()

        val focusResult = audioManager.requestAudioFocus(focusRequest!!)
        if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return
        }

        val uri = if (!uriString.isNullOrBlank()) {
            uriString.toUri()
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }

        val userManager = context.getSystemService(UserManager::class.java)
        val isUserUnlocked = userManager?.isUserUnlocked ?: true
        val isDirectBoot = !isUserUnlocked

        val isContentUri = uri.scheme == "content"

        if (isDirectBoot && isContentUri) {
            Log.d(
                Constants.TAG_AUDIO_PLAYER,
                "Direct Boot active: skipping locked content URI: $uri"
            )
            playFallback(
                attributes,
                loop,
                ringtoneVolume,
                graduallyIncreaseVolume,
                graduallyIncreaseVolumeDuration,
                fallbackUriString
            )
            return
        }

        try {
            startMediaPlayer(
                uri,
                attributes,
                loop,
                ringtoneVolume,
                graduallyIncreaseVolume,
                graduallyIncreaseVolumeDuration
            )
        } catch (e: Exception) {
            Log.w(Constants.TAG_AUDIO_PLAYER, "Error playing alarm from URI: $uri", e)
            playFallback(
                attributes,
                loop,
                ringtoneVolume,
                graduallyIncreaseVolume,
                graduallyIncreaseVolumeDuration,
                fallbackUriString
            )
        }
    }

    private fun playFallback(
        attributes: AudioAttributes,
        loop: Boolean,
        ringtoneVolume: Float?,
        graduallyIncreaseVolume: Boolean,
        graduallyIncreaseVolumeDuration: Int,
        fallbackUriString: String? = null
    ) {
        try {
            val fallbackUri = if (!fallbackUriString.isNullOrBlank()) {
                fallbackUriString.toUri()
            } else {
                "android.resource://${context.packageName}/${R.raw.neptunium}".toUri()
            }
            startMediaPlayer(
                fallbackUri,
                attributes,
                loop,
                ringtoneVolume,
                graduallyIncreaseVolume,
                graduallyIncreaseVolumeDuration
            )
        } catch (fallbackException: Exception) {
            Log.w(Constants.TAG_AUDIO_PLAYER, "Error playing fallback alarm", fallbackException)
        }
    }

    private fun buildAudioAttributes(alarm: Boolean, ringtoneVolume: Float?): AudioAttributes {
        val usage = if (ringtoneVolume != null || !alarm) {
            AudioAttributes.USAGE_MEDIA
        } else {
            AudioAttributes.USAGE_ALARM
        }

        return AudioAttributes.Builder()
            .setUsage(usage)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
    }

    private fun startMediaPlayer(
        uri: Uri,
        attributes: AudioAttributes,
        loop: Boolean,
        ringtoneVolume: Float?,
        graduallyIncreaseVolume: Boolean,
        graduallyIncreaseVolumeDuration: Int
    ) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, uri)
            setAudioAttributes(attributes)
            isLooping = loop
            prepare()

            val targetVolume = ringtoneVolume ?: 1.0f
            if (graduallyIncreaseVolume) {
                setVolume(0f, 0f)
                volumeAnimator = ValueAnimator.ofFloat(0f, targetVolume).apply {
                    duration = graduallyIncreaseVolumeDuration * 1000L
                    addUpdateListener { animator ->
                        val volume = animator.animatedValue as Float
                        mediaPlayer?.let {
                            try {
                                it.setVolume(volume, volume)
                            } catch (e: IllegalStateException) {
                                animator.cancel()
                                Log.d(Constants.TAG_AUDIO_PLAYER, e.toString())
                            }
                        }
                    }
                    start()
                }
            } else {
                ringtoneVolume?.let { setVolume(it, it) }
            }

            start()
        }
    }

    fun stop() {
        try {
            volumeAnimator?.cancel()
            volumeAnimator = null
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: IllegalStateException) {
            Log.d(Constants.TAG_AUDIO_PLAYER, e.toString())
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
            longArrayOf(
                0,
                Constants.Alarm.WAVE_FORM,
                Constants.Alarm.WAVE_FORM
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
