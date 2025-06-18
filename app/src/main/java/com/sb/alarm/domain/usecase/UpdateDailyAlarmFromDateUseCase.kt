package com.sb.alarm.domain.usecase

import com.sb.alarm.domain.model.Alarm
import com.sb.alarm.domain.repository.AlarmRepository
import com.sb.alarm.domain.repository.AlarmSchedulerRepository
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class UpdateDailyAlarmFromDateUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val alarmSchedulerRepository: AlarmSchedulerRepository,
) {
    /**
     * 특정 날짜부터 매일 알람 시간을 변경합니다.
     * 기존 알람은 변경 전날까지 유효하도록 종료하고, 새로운 시간으로 알람을 생성합니다.
     *
     * @param originalAlarm 기존 알람
     * @param hour 새로운 시간 (0-23)
     * @param minute 새로운 분 (0-59)
     * @param startDate 변경 시작 날짜 ("2025-01-15" 형식)
     * @return Result<Unit> 성공 여부
     */
    suspend operator fun invoke(
        originalAlarm: Alarm,
        hour: Int,
        minute: Int,
        startDate: String,
    ): Result<Unit> {
        return try {
            // 시작 날짜를 LocalDate로 파싱하고 타임스탬프로 변환
            val startLocalDate = LocalDate.parse(startDate)
            val startTimestamp =
                startLocalDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000

            // 이전 날짜 계산 (시작 날짜 하루 전)
            val endLocalDate = startLocalDate.minusDays(1)
            val endTimestamp = endLocalDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault())
                .toEpochSecond() * 1000

            // 1. 기존 알람의 종료 날짜를 설정하여 변경 전 날짜까지만 유효하게 함
            val updatedOriginalAlarm = originalAlarm.copy(endDate = endTimestamp)
            alarmRepository.updateAlarm(updatedOriginalAlarm)

            // 2. 기존 알람을 AlarmScheduler에서 취소
            alarmSchedulerRepository.cancel(originalAlarm.id)

            // 3. 새로운 알람을 생성 (선택한 날짜부터 새로운 시간으로 시작)
            val newAlarm = originalAlarm.copy(
                id = 0, // 새 알람이므로 ID 초기화
                hour = hour,
                minute = minute,
                startDate = startTimestamp,
                endDate = null // 새 알람은 종료 날짜 없음 (무기한)
            )
            val newAlarmId = alarmRepository.addAlarm(newAlarm)

            // 4. 새로운 알람을 AlarmScheduler에 등록
            if (newAlarmId > 0) {
                val savedNewAlarm = newAlarm.copy(id = newAlarmId.toInt())
                alarmSchedulerRepository.schedule(savedNewAlarm)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 