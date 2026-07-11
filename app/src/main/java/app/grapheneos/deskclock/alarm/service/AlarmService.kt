package app.grapheneos.deskclock.alarm.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.net.toUri
import app.grapheneos.deskclock.alarm.data.AlarmRepository
import app.grapheneos.deskclock.alarm.util.AlarmConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class AlarmService : Service(), KoinComponent {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val repository: AlarmRepository by inject()
    private val notificationManager: AlarmNotificationManager by inject()
    private val attributedContext: Context by inject(named("AttributedContext"))

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val instanceId = intent?.getLongExtra(AlarmConstants.EXTRA_INSTANCE_ID, -1L) ?: -1L

        val pm = getSystemService(PowerManager::class.java)
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, AlarmConstants.PM_TAG).apply {
            acquire(AlarmConstants.WAKE_LOCK_TIMEOUT)
        }

        notificationManager.setupNotificationChannel()
        val notification = notificationManager.buildAlarmNotification(instanceId)

        startForeground(
            AlarmConstants.NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )

        serviceScope.launch {
            val alarmWithInstance = repository.getAlarmByInstanceId(instanceId)
            val customUri = alarmWithInstance?.alarm?.ringtoneUri
            val shouldVibrate = alarmWithInstance?.alarm?.vibrate ?: true

            requestAudioFocusAndPlay(customUri)
            if (shouldVibrate) startVibration()
        }

        return START_STICKY
    }

    private fun requestAudioFocusAndPlay(customUri: String?) {
        val audioManager = getSystemService(AudioManager::class.java)
        val playbackAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val focusRequest =
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(playbackAttributes)
                .build()

        val result = audioManager.requestAudioFocus(focusRequest)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            playAlarmSound(customUri, playbackAttributes)
        }
    }

    private fun playAlarmSound(customUriString: String?, attributes: AudioAttributes) {
        val uri =
            customUriString?.toUri() ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(attributedContext, uri)
                setAudioAttributes(attributes)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            if (uri != RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)) {
                playAlarmSound(null, attributes)
            }
        }
    }

    private fun startVibration() {
        val vibratorManager = getSystemService(VibratorManager::class.java)
        vibrator = vibratorManager.defaultVibrator
        val effect = VibrationEffect.createWaveform(longArrayOf(0, 1000, 1000), 0)
        val attributes = VibrationAttributes.Builder()
            .setUsage(VibrationAttributes.USAGE_ALARM).build()
        vibrator?.vibrate(effect, attributes)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        vibrator?.cancel()
        if (wakeLock?.isHeld == true) wakeLock?.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
