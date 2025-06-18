package com.sb.alarm.domain.usecase

import com.sb.alarm.domain.model.Alarm
import com.sb.alarm.domain.repository.AlarmRepository
import com.sb.alarm.domain.repository.AlarmSchedulerRepository
import com.sb.alarm.presentation.updateSchedule.AlternatingStep
import com.sb.alarm.shared.constants.RepeatType
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class UpdateAlternatingAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val alarmSchedulerRepository: AlarmSchedulerRepository,
) {
    /**
     * 특정 날짜부터 교대형 알람을 설정합니다.
     * 기존 알람은 변경 전날까지 유효하도록 종료하고, 교대형 패턴으로 새로운 알람들을 생성합니다.
     *
     * @param originalAlarm 기존 알람
     * @param alternatingSteps 교대형 알람 패턴
     * @param startDate 변경 시작 날짜 ("2025-01-15" 형식)
     * @return Result<Unit> 성공 여부
     */
    suspend operator fun invoke(
        originalAlarm: Alarm,
        alternatingSteps: List<AlternatingStep>,
        startDate: String,
    ): Result<Unit> {
        return try {
            // 시작 날짜 파싱
            val startLocalDate = LocalDate.parse(startDate)

            // 1. 기존 알람 종료
            val endLocalDate = startLocalDate.minusDays(1)
            val endTimestamp = endLocalDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault())
                .toEpochSecond() * 1000
            val updatedOriginalAlarm = originalAlarm.copy(endDate = endTimestamp)
            alarmRepository.updateAlarm(updatedOriginalAlarm)

            // 2. 새로운 교대형 알람들 생성
            val totalCycleDays = getTotalCycleDays(alternatingSteps)
            var currentDay = 0
            val createdAlarms = mutableListOf<Alarm>()

            alternatingSteps.forEach { step ->
                repeat(step.durationDays) { dayIndex ->
                    step.times.forEach { alarmTime ->
                        val alarmStartDate = startLocalDate.plusDays(currentDay.toLong())
                        val alarmStartTimestamp =
                            alarmStartDate.atStartOfDay(ZoneId.systemDefault())
                                .toEpochSecond() * 1000

                        val newAlarm = originalAlarm.copy(
                            id = 0, // 새 알람
                            hour = alarmTime.hour,
                            minute = alarmTime.minute,
                            repeatType = RepeatType.DAYS_INTERVAL,
                            repeatInterval = totalCycleDays, // 전체 주기 일수
                            startDate = alarmStartTimestamp,
                            endDate = null
                        )
                        val newAlarmId = alarmRepository.addAlarm(newAlarm)

                        // 3. 새로운 알람을 AlarmScheduler에 등록
                        if (newAlarmId > 0) {
                            val savedNewAlarm = newAlarm.copy(id = newAlarmId.toInt())
                            createdAlarms.add(savedNewAlarm)
                            alarmSchedulerRepository.schedule(savedNewAlarm)
                        }
                    }
                    currentDay++
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getTotalCycleDays(alternatingSteps: List<AlternatingStep>): Int {
        return alternatingSteps.sumOf { it.durationDays }
    }
} 