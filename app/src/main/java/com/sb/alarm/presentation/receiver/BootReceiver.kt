package com.sb.alarm.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sb.alarm.domain.usecase.RescheduleAllActiveAlarmsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var rescheduleAllActiveAlarmsUseCase: RescheduleAllActiveAlarmsUseCase

    // BroadcastReceiver의 생명주기에 맞는 코루틴 스코프
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                Log.i("BootReceiver", "Device booted or app updated, rescheduling alarms...")
                
                // 비동기적으로 알람 재스케줄링 실행
                val pendingResult = goAsync()
                
                scope.launch {
                    try {
                        val result = rescheduleAllActiveAlarmsUseCase()
                        
                        result.onSuccess { count ->
                            Log.i("BootReceiver", "Successfully rescheduled $count alarms after boot")
                        }.onFailure { error ->
                            Log.e("BootReceiver", "Failed to reschedule alarms after boot", error)
                        }
                        
                    } catch (e: Exception) {
                        Log.e("BootReceiver", "Unexpected error during alarm rescheduling", e)
                    } finally {
                        // BroadcastReceiver 작업 완료 신호
                        pendingResult.finish()
                    }
                }
            }
            
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                Log.i("BootReceiver", "Time/timezone changed, rescheduling alarms...")
                
                val pendingResult = goAsync()
                
                scope.launch {
                    try {
                        val result = rescheduleAllActiveAlarmsUseCase()
                        
                        result.onSuccess { count ->
                            Log.i("BootReceiver", "Successfully rescheduled $count alarms after time change")
                        }.onFailure { error ->
                            Log.e("BootReceiver", "Failed to reschedule alarms after time change", error)
                        }
                        
                    } catch (e: Exception) {
                        Log.e("BootReceiver", "Unexpected error during time change rescheduling", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            
            else -> {
                Log.d("BootReceiver", "Received unhandled action: ${intent.action}")
            }
        }
    }
} 