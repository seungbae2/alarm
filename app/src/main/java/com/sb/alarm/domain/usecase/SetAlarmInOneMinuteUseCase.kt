package com.sb.alarm.domain.usecase

import com.sb.alarm.domain.model.AlarmWithStatus
import com.sb.alarm.domain.repository.AlarmRepository
import com.sb.alarm.domain.repository.AlarmSchedulerRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import javax.inject.Inject

class SetAlarmInOneMinuteUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val alarmSchedulerRepository: AlarmSchedulerRepository,
) {
    /**
     * 1분 후에 알람이 울리도록 설정합니다.
     * 
     * @param alarmWithStatus 알람 정보
     * @return Result<String> 성공 시 1분 후 시간 문자열, 실패 시 에러
     */
    suspend operator fun invoke(alarmWithStatus: AlarmWithStatus): Result<String> {
        return try {
            // 1. 현재 시간 + 1분 계산
            val currentTime = Clock.System.now()
            val oneMinuteLater = currentTime.plus(1, DateTimeUnit.MINUTE)
            val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val oneMinuteLaterTimeString = "${
                oneMinuteLater.toLocalDateTime(TimeZone.currentSystemDefault()).hour.toString()
                    .padStart(2, '0')
            }:${
                oneMinuteLater.toLocalDateTime(TimeZone.currentSystemDefault()).minute.toString()
                    .padStart(2, '0')
            }"

            // 2. 히스토리에 1분뒤 알람 정보 저장
            alarmRepository.saveOneMinuteLaterHistory(
                alarmId = alarmWithStatus.alarm.id,
                date = currentDate.toString(),
                oneMinuteLaterTime = oneMinuteLaterTimeString
            )

            // 3. AlarmManager에 1분 후 알람 등록
            alarmSchedulerRepository.scheduleOneTimeAlarm(alarmWithStatus.alarm)

            Result.success(oneMinuteLaterTimeString)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 