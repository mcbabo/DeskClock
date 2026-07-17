package app.grapheneos.deskclock.alarm.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.core.net.toUri

class AlarmSoundPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun playPreview(uriString: String) {
        stop()
        try {
            val uri = uriString.toUri()

            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.d("AlarmSoundPlayer", e.toString())
        }
    }

    fun stop() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }
}
