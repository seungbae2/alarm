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
        const val ACTION_SNOOZE_ALARM = "com.sb.alarm.ACTION_SNOOZE_ALARM"
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
                startForegroundService(alarmId, medicationName)

                // 알람 시작
                startAlarm(alarmId, medicationName)
            }

            ACTION_STOP_ALARM -> {
                Log.d("AlarmService", "Stopping alarm service")
                stopAlarm()
            }

            ACTION_SNOOZE_ALARM -> {
                Log.d("AlarmService", "Snoozing alarm service")
                stopAlarm()
            }
        }
        return START_STICKY
    }

    private fun startForegroundService(alarmId: Int, medicationName: String) {
        val notificationManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getSystemService(NotificationManager::class.java)
        } else {
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        }

        // 알림 채널 생성 (Android 8.0 이상)
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
                    setBypassDnd(true) // 방해금지 모드 무시
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
            }
        }

        // Full-screen intent로 잠금 화면에서도 표시
        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("MEDICATION_NAME", medicationName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId,
            fullScreenIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        // 알람 중지 액션 추가
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            alarmId + 1000,
            stopIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        // 스누즈 액션 추가
        val snoozeIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_SNOOZE_ALARM
        }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            alarmId + 2000,
            snoozeIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ $medicationName 복용 시간입니다!")
            .setContentText("지금 복용하세요")
            .setPriority(NotificationCompat.PRIORITY_MAX) // 최대 우선순위
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true) // Full-screen intent
            .setContentIntent(fullScreenPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 잠금 화면에서 표시
            .addAction(
                android.R.drawable.ic_media_pause,
                "중지",
                stopPendingIntent
            )
            .addAction(
                android.R.drawable.ic_media_next,
                "5분 후",
                snoozePendingIntent
            )
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
            // 1. 화면 깨우기 (더 강력한 WakeLock 사용)
            acquireWakeLock()

            // 2. 진동 시작
            startVibration()

            // 3. 알람 소리 재생
            playAlarmSound()

            // 4. 지속성 모니터링 서비스 시작
            startPersistentMonitoring(alarmId, medicationName)

            // 5. 알람 액티비티 실행 (잠금 화면 대응)
            launchAlarmActivity(alarmId, medicationName)

        } catch (e: Exception) {
            Log.e("AlarmService", "Error starting alarm $alarmId", e)
            // 에러 발생 시에도 액티비티는 실행하려고 시도
            try {
                launchAlarmActivity(alarmId, medicationName)
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

            // PARTIAL_WAKE_LOCK과 화면 켜기 플래그 조합 사용
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or
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

            // 더 강한 진동 패턴: [대기, 진동, 대기, 진동] (밀리초)
            val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)

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

    private fun launchAlarmActivity(alarmId: Int, medicationName: String) {
        try {
            val intent = Intent(this, AlarmActivity::class.java).apply {
                putExtra("ALARM_ID", alarmId)
                putExtra("MEDICATION_NAME", medicationName)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                        Intent.FLAG_ACTIVITY_NO_HISTORY
            }
            startActivity(intent)
            Log.d("AlarmService", "Alarm activity launched for ID: $alarmId")
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to launch alarm activity for ID: $alarmId", e)
            // Activity 실행 실패 시 알림이 이미 표시되고 있으므로 추가 처리 불필요
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

            // 4. 지속성 모니터링 서비스 중지
            stopPersistentMonitoring()

            Log.d("AlarmService", "Alarm stopped successfully (sound and vibration)")

            // 5. 포그라운드 상태 해제 및 서비스 종료
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

    private fun startPersistentMonitoring(alarmId: Int, medicationName: String) {
        try {
            val intent = Intent(this, PersistentAlarmService::class.java).apply {
                action = PersistentAlarmService.ACTION_START_MONITORING
                putExtra("ALARM_ID", alarmId)
                putExtra("MEDICATION_NAME", medicationName)
            }
            startService(intent)
            Log.d("AlarmService", "Started persistent monitoring service for alarm ID: $alarmId")
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to start persistent monitoring service", e)
        }
    }

    private fun stopPersistentMonitoring() {
        try {
            val intent = Intent(this, PersistentAlarmService::class.java).apply {
                action = PersistentAlarmService.ACTION_STOP_MONITORING
            }
            startService(intent)
            Log.d("AlarmService", "Stopped persistent monitoring service")
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to stop persistent monitoring service", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
} 