package app.grapheneos.deskclock.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.util.Log
import androidx.core.net.toUri
import java.io.IOException

class AudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var focusRequest: AudioFocusRequest? = null
    private val audioManager = context.getSystemService(AudioManager::class.java)

    fun playAlarm(
        uriString: String?,
        loop: Boolean = true,
        alarm: Boolean = true,
        ringtoneVolume: Float? = null
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

        try {
            startMediaPlayer(uri, attributes, loop, ringtoneVolume)
        } catch (e: IOException) {
            Log.e("AudioPlayer", "Error playing alarm", e)
            if (uriString != null) {
                playAlarm(null, loop, alarm, ringtoneVolume)
            }
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
        uri: android.net.Uri,
        attributes: AudioAttributes,
        loop: Boolean,
        ringtoneVolume: Float?
    ) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, uri)
            setAudioAttributes(attributes)
            isLooping = loop
            prepare()
            ringtoneVolume?.let { setVolume(it, it) }
            start()
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: IllegalStateException) {
            Log.d("AudioPlayer", e.toString())
        } finally {
            mediaPlayer = null
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        }
    }
}
