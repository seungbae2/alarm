package com.sb.alarm.presentation.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sb.alarm.presentation.alarm.AlarmActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PersistentAlarmService : Service() {

    private var alarmId: Int = -1
    private var medicationName: String = ""
    private var monitoringHandler: Handler? = null
    private var isAlarmActive = false

    companion object {
        const val ACTION_START_MONITORING = "START_MONITORING"
        const val ACTION_STOP_MONITORING = "STOP_MONITORING"
        private const val MONITORING_INTERVAL = 2000L // 2초마다 체크 (빠른 반응)
        private const val PERSISTENT_NOTIFICATION_ID = 1002
        private const val PERSISTENT_CHANNEL_ID = "persistent_alarm_channel"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MONITORING -> {
                alarmId = intent.getIntExtra("ALARM_ID", -1)
                medicationName = intent.getStringExtra("MEDICATION_NAME") ?: ""
                
                // 포그라운드 서비스로 시작
                startForegroundService()
                startMonitoring()
                
                Log.d("PersistentAlarmService", "Started persistent monitoring for alarm ID: $alarmId")
            }
            ACTION_STOP_MONITORING -> {
                stopMonitoring()
                Log.d("PersistentAlarmService", "Stopped persistent monitoring")
            }
        }
        return START_STICKY // 시스템에 의해 종료되어도 자동 재시작
    }

    private fun startForegroundService() {
        val notificationManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getSystemService(NotificationManager::class.java)
        } else {
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        }

        // 알림 채널 생성 (Android 8.0 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existingChannel = notificationManager.getNotificationChannel(PERSISTENT_CHANNEL_ID)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    PERSISTENT_CHANNEL_ID,
                    "알람 지속 서비스",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "알람이 지속적으로 표시되도록 하는 서비스"
                    enableVibration(false)
                    setSound(null, null)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }

        // 알람 Activity로 이동하는 PendingIntent
        val intent = Intent(this, AlarmActivity::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("MEDICATION_NAME", medicationName)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            alarmId,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val notification = NotificationCompat.Builder(this, PERSISTENT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("알람 활성화 중")
            .setContentText("$medicationName 복용 알람이 활성화되어 있습니다")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        // 포그라운드 서비스 시작
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                PERSISTENT_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(PERSISTENT_NOTIFICATION_ID, notification)
        }
    }

    private fun startMonitoring() {
        isAlarmActive = true
        monitoringHandler = Handler(Looper.getMainLooper())
        
        val monitoringRunnable = object : Runnable {
            override fun run() {
                if (isAlarmActive) {
                    Log.d("PersistentAlarmService", "Monitoring: Ensuring alarm activity is visible...")
                    launchAlarmActivity()
                    
                    // 다음 체크 스케줄링
                    monitoringHandler?.postDelayed(this, MONITORING_INTERVAL)
                }
            }
        }
        
        // 즉시 첫 번째 실행
        monitoringHandler?.post(monitoringRunnable)
    }

    private fun launchAlarmActivity() {
        try {
            val intent = Intent(this, AlarmActivity::class.java).apply {
                putExtra("ALARM_ID", alarmId)
                putExtra("MEDICATION_NAME", medicationName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                        Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
            }
            startActivity(intent)
            Log.d("PersistentAlarmService", "Launched AlarmActivity for alarm ID: $alarmId")
        } catch (e: Exception) {
            Log.e("PersistentAlarmService", "Failed to launch AlarmActivity for alarm ID: $alarmId", e)
            
            // 실패 시 다른 방법으로 시도
            try {
                val fallbackIntent = Intent(this, AlarmActivity::class.java).apply {
                    putExtra("ALARM_ID", alarmId)
                    putExtra("MEDICATION_NAME", medicationName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(fallbackIntent)
                Log.d("PersistentAlarmService", "Fallback launch successful for alarm ID: $alarmId")
            } catch (e2: Exception) {
                Log.e("PersistentAlarmService", "Fallback launch also failed for alarm ID: $alarmId", e2)
            }
        }
    }

    private fun stopMonitoring() {
        isAlarmActive = false
        monitoringHandler?.removeCallbacksAndMessages(null)
        monitoringHandler = null
        
        // 포그라운드 상태 해제
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("PersistentAlarmService", "PersistentAlarmService destroyed")
        stopMonitoring()
    }

    override fun onBind(intent: Intent?): IBinder? = null
} 