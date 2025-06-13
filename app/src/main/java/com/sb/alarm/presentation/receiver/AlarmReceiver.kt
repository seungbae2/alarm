package com.sb.alarm.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.sb.alarm.presentation.service.AlarmService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val medicationName = intent.getStringExtra("MEDICATION_NAME") ?: "알 수 없는 약물"

        if (alarmId == -1) {
            Log.w("AlarmReceiver", "Invalid alarm ID received")
            return
        }

        Log.i("AlarmReceiver", "Alarm received for ID: $alarmId, starting AlarmService")

        // AlarmService 시작 Intent 생성
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            action = AlarmService.ACTION_START_ALARM
            putExtra("ALARM_ID", alarmId)
            putExtra("MEDICATION_NAME", medicationName)
        }

        try {
            // Android 8.0 이상에서는 foregroundService로 시작해야 함
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            Log.d("AlarmReceiver", "AlarmService started successfully for alarm ID: $alarmId")
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to start AlarmService for alarm ID: $alarmId", e)
        }
    }
}