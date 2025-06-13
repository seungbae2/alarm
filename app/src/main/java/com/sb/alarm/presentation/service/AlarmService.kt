package com.sb.alarm.presentation.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sb.alarm.presentation.alarm.AlarmActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        const val ACTION_START_ALARM = "com.sb.alarm.ACTION_START_ALARM"
        const val ACTION_STOP_ALARM = "com.sb.alarm.ACTION_STOP_ALARM"
        private const val NOTIFICATION_ID = 1001
        private const val NOTIFICATION_CHANNEL_ID = "alarm_service_channel"
        private const val WAKE_LOCK_TAG = "AlarmApp:AlarmServiceWakeLock"
        private const val WAKE_LOCK_TIMEOUT = 10 * 60 * 1000L // 10분
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_ALARM -> {
                val alarmId = intent.getIntExtra("ALARM_ID", -1)
                val medicationName = intent.getStringExtra("MEDICATION_NAME") ?: "알 수 없는 약물"

                Log.d(
                    "AlarmService",
                    "Starting alarm for ID: $alarmId, medication: $medicationName"
                )

                // 포그라운드 서비스 시작
                startForegroundService(medicationName)

                // 알람 시작
                startAlarm(alarmId, medicationName)
            }

            ACTION_STOP_ALARM -> {
                Log.d("AlarmService", "Stopping alarm service")
                stopAlarm()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundService(medicationName: String) {
        val notificationManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getSystemService(NotificationManager::class.java)
        } else {
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        }

        // Android 8.0 이상에서는 알림 채널 생성 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existingChannel =
                notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "알람 서비스",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "알람이 울리는 동안 표시되는 서비스 알림"
                    enableVibration(false) // 진동은 별도로 처리
                    setSound(null, null) // 소리도 별도로 처리
                }
                notificationManager.createNotificationChannel(channel)
            }
        }

        // 알람 Activity로 이동하는 PendingIntent
        val intent = Intent(this, AlarmActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ $medicationName 복용 시간입니다!")
            .setContentText("알람이 울리고 있습니다")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        // Android 14+ (API 34)에서는 서비스 타입을 명시해야 함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startAlarm(alarmId: Int, medicationName: String) {
        try {
            // 1. 화면 깨우기
            acquireWakeLock()

            // 2. 진동 시작
            startVibration()

            // 3. 알람 소리 재생
            playAlarmSound()

            // 4. 알람 액티비티 실행
            launchAlarmActivity(alarmId)

        } catch (e: Exception) {
            Log.e("AlarmService", "Error starting alarm $alarmId", e)
            // 에러 발생 시에도 액티비티는 실행하려고 시도
            try {
                launchAlarmActivity(alarmId)
            } catch (e2: Exception) {
                Log.e("AlarmService", "Failed to launch alarm activity as fallback", e2)
            }
        }
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getSystemService(PowerManager::class.java)
            } else {
                getSystemService(POWER_SERVICE) as PowerManager
            }

            wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                        PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.ON_AFTER_RELEASE,
                WAKE_LOCK_TAG
            )
            wakeLock?.acquire(WAKE_LOCK_TIMEOUT)
            Log.d("AlarmService", "Wake lock acquired")
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to acquire wake lock", e)
        }
    }

    private fun startVibration() {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(VibratorManager::class.java)
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }

            // 진동 패턴: [대기, 진동, 대기, 진동] (밀리초)
            val pattern = longArrayOf(0, 500, 200, 500, 200, 500)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(pattern, 0),
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
            Log.d("AlarmService", "Vibration started")
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to start vibration", e)
            vibrator = null
        }
    }

    private fun playAlarmSound() {
        try {
            mediaPlayer?.release() // 기존 재생 중인 소리 정리

            mediaPlayer = MediaPlayer().apply {
                // 기본 알람 소리 사용
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

                if (alarmUri == null) {
                    Log.w("AlarmService", "No alarm sound available")
                    return@apply
                }

                setDataSource(this@AlarmService, alarmUri)

                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )

                isLooping = true // 반복 재생
                prepare()
                start()
            }
            Log.d("AlarmService", "Alarm sound started")
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to play alarm sound", e)
        }
    }

    private fun launchAlarmActivity(alarmId: Int) {
        try {
            val intent = Intent(this, AlarmActivity::class.java).apply {
                putExtra("ALARM_ID", alarmId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_NO_USER_ACTION
            }
            startActivity(intent)
            Log.d("AlarmService", "Alarm activity launched for ID: $alarmId")
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to launch alarm activity for ID: $alarmId", e)
            throw e
        }
    }

    private fun stopAlarm() {
        try {
            // 1. MediaPlayer 중지
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null

            // 2. 진동 중지
            vibrator?.apply {
                cancel()
            }
            vibrator = null

            // 3. WakeLock 해제
            wakeLock?.apply {
                if (isHeld) {
                    release()
                }
            }
            wakeLock = null

            Log.d("AlarmService", "Alarm stopped successfully (sound and vibration)")

            // 4. 포그라운드 상태 해제 및 서비스 종료
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            stopSelf()

        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to stop alarm", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AlarmService", "AlarmService destroyed")
        // 서비스가 어떤 이유로든 종료될 때 알람을 확실히 멈춤
        stopAlarm()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
} 