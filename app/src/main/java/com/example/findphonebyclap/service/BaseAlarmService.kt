package com.example.findphonebyclap.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.findphonebyclap.MainActivity
import com.example.findphonebyclap.R
import com.example.findphonebyclap.data.model.FunctionMode
import com.example.findphonebyclap.data.prefs.AppPreferences
import com.example.findphonebyclap.data.repository.SoundRepository
import com.example.findphonebyclap.engine.FlashlightController
import com.example.findphonebyclap.engine.VibrationController
import com.example.findphonebyclap.ui.alarm.FoundPhoneActivity
import com.example.findphonebyclap.util.Constants

abstract class BaseAlarmService : Service() {

    protected val prefs by lazy { AppPreferences.getInstance(this) }
    protected val soundRepository by lazy { SoundRepository(this) }
    protected val flashlightController by lazy { FlashlightController(this) }
    protected val vibrationController by lazy { VibrationController(this) }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var alarmPlayer: MediaPlayer? = null
    private var countDownTimer: CountDownTimer? = null
    private var originalVolume: Int = -1
    private var isAlarmTriggered: Boolean = false
    private var wakeLock: PowerManager.WakeLock? = null

    abstract fun getServiceMode(): FunctionMode

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == Constants.ACTION_STOP_SERVICE || prefs.activeMode != getServiceMode()) {
            stopAlarm()
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent?.action == Constants.ACTION_STOP_ALARM) {
            stopAlarm()
            return START_STICKY
        }

        startForeground(Constants.NOTIFICATION_ID, buildForegroundNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.CHANNEL_ID,
                Constants.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notification to keep detection service running and trigger alerts"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    protected fun buildForegroundNotification(isAlarming: Boolean = false, mode: FunctionMode? = null): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, this::class.java).apply {
            action = Constants.ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isAlarming) {
            when (mode ?: getServiceMode()) {
                FunctionMode.MODE_CLAP_FIND_PHONE -> getString(R.string.phone_found_title)
                FunctionMode.MODE_NOT_TOUCH_PHONE -> getString(R.string.motion_detected_title)
                FunctionMode.MODE_ANTI_PICKPOCKET -> getString(R.string.pocket_detected_title)
                else -> getString(R.string.phone_found_title)
            }
        } else {
            when (getServiceMode()) {
                FunctionMode.MODE_CLAP_FIND_PHONE -> getString(R.string.status_clap_active)
                FunctionMode.MODE_NOT_TOUCH_PHONE -> getString(R.string.status_touch_active)
                FunctionMode.MODE_ANTI_PICKPOCKET -> getString(R.string.status_pocket_active)
                else -> getString(R.string.notification_service_title)
            }
        }

        val builder = NotificationCompat.Builder(this, Constants.CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(title)
            .setSmallIcon(R.drawable.boomclap_ic_activate)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.boomclap_ic_deactivate, getString(R.string.tap_to_deactivate), stopPendingIntent)
            .setOngoing(true)
            .setPriority(if (isAlarming) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_LOW)

        if (isAlarming) {
            val alarmIntent = Intent(this, FoundPhoneActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(Constants.EXTRA_FUNCTION_MODE, (mode ?: getServiceMode()).name)
            }
            val alarmPendingIntent = PendingIntent.getActivity(
                this, 2, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setFullScreenIntent(alarmPendingIntent, true)
        }

        return builder.build()
    }

    fun triggerAlarm(mode: FunctionMode) {
        mainHandler.post {
            performTriggerAlarm(mode)
        }
    }

    private fun performTriggerAlarm(mode: FunctionMode) {
        if (isAlarmTriggered) return
        isAlarmTriggered = true

        acquireWakeLock()
        updateNotificationForAlarm(mode)
        openFoundPhoneScreen(mode)
        adjustAudioVolume()
        startAudioPlayback()

        if (prefs.isFlashlightEnabled) {
            flashlightController.start(prefs.flashlightMode)
        }

        if (prefs.isVibrationEnabled) {
            vibrationController.start(prefs.vibrationMode)
        }

        val durationSeconds = prefs.alarmDurationSeconds
        if (durationSeconds > 0) {
            countDownTimer?.cancel()
            countDownTimer = object : CountDownTimer(durationSeconds * 1000L, 1000L) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    performStopAlarm()
                }
            }.start()
        }
    }

    private fun updateNotificationForAlarm(mode: FunctionMode) {
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(Constants.NOTIFICATION_ID, buildForegroundNotification(isAlarming = true, mode = mode))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAlarm() {
        mainHandler.post {
            performStopAlarm()
        }
    }

    private fun performStopAlarm() {
        if (!isAlarmTriggered) return
        isAlarmTriggered = false

        countDownTimer?.cancel()
        countDownTimer = null

        stopAudioPlayback()
        flashlightController.stop()
        vibrationController.stop()
        restoreAudioVolume()
        releaseWakeLock()

        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(Constants.NOTIFICATION_ID, buildForegroundNotification(isAlarming = false))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        FoundPhoneActivity.dismissActiveScreen()
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "findphonebyclap:AlarmWakeLock"
            )?.apply {
                acquire(3 * 60 * 1000L)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            wakeLock = null
        }
    }

    private fun openFoundPhoneScreen(mode: FunctionMode) {
        try {
            val intent = Intent(this, FoundPhoneActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(Constants.EXTRA_FUNCTION_MODE, mode.name)
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun adjustAudioVolume() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.let {
                originalVolume = it.getStreamVolume(AudioManager.STREAM_MUSIC)
                val maxVolume = it.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val targetVolume = ((prefs.alarmVolumePercent / 100.0f) * maxVolume).toInt().coerceIn(1, maxVolume)
                it.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun restoreAudioVolume() {
        if (originalVolume != -1) {
            try {
                val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            originalVolume = -1
        }
    }

    private fun startAudioPlayback() {
        stopAudioPlayback()
        val soundItem = soundRepository.getSoundById(prefs.selectedSoundId)
        try {
            val afd: AssetFileDescriptor = assets.openFd("audio/${soundItem.audioFileName}")
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                isLooping = true
                prepare()
                start()
            }
            alarmPlayer = player
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAudioPlayback() {
        try {
            alarmPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            alarmPlayer = null
        }
    }

    override fun onDestroy() {
        performStopAlarm()
        mainHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
