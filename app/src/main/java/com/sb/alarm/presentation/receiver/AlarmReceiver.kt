package com.sb.alarm.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.sb.alarm.presentation.service.AlarmService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val medicationName = intent.getStringExtra("MEDICATION_NAME") ?: "알 수 없는 약물"
        val repeatType = intent.getStringExtra("REPEAT_TYPE") ?: ""
        val isOneMinuteLater = intent.getBooleanExtra("IS_ONE_MINUTE_LATER", false)

        if (alarmId == -1) {
            Log.w("AlarmReceiver", "Invalid alarm ID received")
            return
        }

        if (isOneMinuteLater) {
            Log.i("AlarmReceiver", "One-minute-later alarm received for ID: $alarmId, medication: $medicationName")
        } else {
            Log.i("AlarmReceiver", "Regular alarm received for ID: $alarmId, medication: $medicationName, repeatType: $repeatType")
        }

        // 백그라운드 처리를 위한 goAsync() 사용
        val pendingResult = goAsync()

        // 코루틴을 사용하여 비동기 처리
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // AlarmService 시작 Intent 생성
                val serviceIntent = Intent(context, AlarmService::class.java).apply {
                    action = AlarmService.ACTION_START_ALARM
                    putExtra("ALARM_ID", alarmId)
                    putExtra("MEDICATION_NAME", medicationName)
                    putExtra("REPEAT_TYPE", repeatType)
                    putExtra("IS_ONE_MINUTE_LATER", isOneMinuteLater)
                }

                // Android 8.0 이상에서는 foregroundService로 시작해야 함
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                
                Log.d("AlarmReceiver", "AlarmService started successfully for alarm ID: $alarmId")
                
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Failed to start AlarmService for alarm ID: $alarmId", e)
            } finally {
                // 비동기 처리 완료
                pendingResult.finish()
            }
        }
    }
}