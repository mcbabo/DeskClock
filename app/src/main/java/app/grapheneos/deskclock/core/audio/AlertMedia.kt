package app.grapheneos.deskclock.core.audio

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
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
        uri: Uri?,
        loop: Boolean = true,
        alarm: Boolean = true,
        ringtoneVolume: Float? = null,
        graduallyIncreaseVolume: Boolean = false,
        graduallyIncreaseVolumeDuration: Int = 30,
        fallbackUri: Uri? = null
    ) {
        stop()

        val attributes = buildAudioAttributes(alarm)
        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(attributes)
            .build()

        val focusResult = audioManager.requestAudioFocus(focusRequest!!)
        if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.w(
                Constants.TAG_AUDIO_PLAYER,
                "Audio focus request denied, but proceeding with alarm."
            )
        }

        val targetUri = uri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: "android.resource://${context.packageName}/raw/neptunium".toUri()

        val userManager = context.getSystemService(UserManager::class.java)
        val isUserUnlocked = userManager?.isUserUnlocked ?: true

        val isSafeScheme = targetUri.scheme == "android.resource" || targetUri.scheme == "file"
        if (!isUserUnlocked && !isSafeScheme) {
            Log.d(
                Constants.TAG_AUDIO_PLAYER,
                "Direct Boot: Content URI $targetUri is locked. Using fallback."
            )
            playFallback(
                attributes,
                loop,
                ringtoneVolume,
                graduallyIncreaseVolume,
                graduallyIncreaseVolumeDuration,
                fallbackUri
            )
            return
        }

        try {
            startMediaPlayer(
                targetUri,
                attributes,
                loop,
                ringtoneVolume,
                graduallyIncreaseVolume,
                graduallyIncreaseVolumeDuration
            )
        } catch (e: Exception) {
            Log.w(Constants.TAG_AUDIO_PLAYER, "Failed to play URI: $uri", e)
            playFallback(
                attributes,
                loop,
                ringtoneVolume,
                graduallyIncreaseVolume,
                graduallyIncreaseVolumeDuration,
                fallbackUri
            )
        }
    }

    private fun playFallback(
        attributes: AudioAttributes,
        loop: Boolean,
        ringtoneVolume: Float?,
        graduallyIncreaseVolume: Boolean,
        graduallyIncreaseVolumeDuration: Int,
        fallbackUri: Uri? = null
    ) {
        try {
            val targetFallbackUri =
                fallbackUri ?: "android.resource://${context.packageName}/raw/neptunium".toUri()
            startMediaPlayer(
                targetFallbackUri,
                attributes,
                loop,
                ringtoneVolume,
                graduallyIncreaseVolume,
                graduallyIncreaseVolumeDuration
            )
        } catch (e: Exception) {
            Log.e(Constants.TAG_AUDIO_PLAYER, "Critical: Fallback also failed", e)
        }
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
            when (uri.scheme) {
                "android.resource" -> {
                    val resName = uri.lastPathSegment
                    val resId = if (resName?.toIntOrNull() != null) {
                        resName.toInt()
                    } else {
                        context.resources.getIdentifier(resName, "raw", context.packageName)
                    }

                    if (resId != 0) {
                        val afd = context.resources.openRawResourceFd(resId)
                        setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        afd.close()
                    } else {
                        throw Resources.NotFoundException("Raw resource not found: $resName")
                    }
                }

                "file" -> {
                    setDataSource(uri.path)
                }

                else -> {
                    setDataSource(context, uri)
                }
            }

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
                        try {
                            mediaPlayer?.setVolume(volume, volume)
                        } catch (_: Exception) {
                            animator.cancel()
                        }
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
