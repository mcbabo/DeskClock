package app.grapheneos.deskclock.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.util.Log
import androidx.core.net.toUri

class AudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var focusRequest: AudioFocusRequest? = null
    private val audioManager = context.getSystemService(AudioManager::class.java)

    fun playAlarm(uriString: String?, loop: Boolean = true) {
        stop()

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(attributes)
            .build()

        if (audioManager.requestAudioFocus(focusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            val uri =
                uriString?.toUri() ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, uri)
                    setAudioAttributes(attributes)
                    isLooping = loop
                    prepare()
                    start()
                }
            } catch (e: java.io.IOException) {
                Log.e("AudioPlayer", "Error playing alarm", e)
                if (uriString != null) playAlarm(null, loop)
            }
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
