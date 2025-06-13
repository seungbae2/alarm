package com.sb.alarm.domain.usecase

import android.annotation.SuppressLint
import android.util.Log
import com.sb.alarm.domain.repository.AlarmRepository
import com.sb.alarm.domain.repository.AlarmSchedulerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RescheduleAllActiveAlarmsUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val alarmSchedulerRepository: AlarmSchedulerRepository,
) {
    /**
     * 모든 활성 알람을 재스케줄링합니다.
     * 주로 재부팅 후 또는 시스템 설정 변경 후 호출됩니다.
     *
     * @return 성공적으로 재스케줄링된 알람 개수
     */
    @SuppressLint("LongLogTag")
    suspend operator fun invoke(): Result<Int> = withContext(Dispatchers.IO) {
        return@withContext try {
            // 모든 활성 알람 조회 (targetDate = null이면 모든 알람)
            val activeAlarms = alarmRepository.getActiveAlarms().first()

            var successCount = 0
            var errorCount = 0
            val errors = mutableListOf<Exception>()

            // 각 알람을 개별적으로 스케줄링 (하나 실패해도 나머지 계속 진행)
            activeAlarms.forEach { alarm ->
                try {
                    alarmSchedulerRepository.schedule(alarm)
                    successCount++
                } catch (e: Exception) {
                    errorCount++
                    errors.add(e)
                    // 개별 알람 스케줄링 실패는 로그만 남기고 계속 진행
                    Log.e(
                        "RescheduleAllActiveAlarms",
                        "Failed to reschedule alarm ${alarm.id}: ${alarm.medicationName}",
                        e
                    )
                }
            }

            // 결과 판정
            when {
                errorCount == 0 -> {
                    Log.i(
                        "RescheduleAllActiveAlarms",
                        "Successfully rescheduled all $successCount active alarms"
                    )
                    Result.success(successCount)
                }

                successCount > 0 -> {
                    Log.w(
                        "RescheduleAllActiveAlarms",
                        "Partially successful: $successCount succeeded, $errorCount failed"
                    )
                    // 일부 성공인 경우에도 성공으로 처리하되 경고 로그
                    Result.success(successCount)
                }

                else -> {
                    Log.e(
                        "RescheduleAllActiveAlarms",
                        "Failed to reschedule any alarms (${activeAlarms.size} total)"
                    )
                    Result.failure(
                        Exception("Failed to reschedule all alarms: ${errors.firstOrNull()?.message}")
                    )
                }
            }

        } catch (e: Exception) {
            Log.e("RescheduleAllActiveAlarms", "Critical error during rescheduling", e)
            Result.failure(e)
        }
    }
} 