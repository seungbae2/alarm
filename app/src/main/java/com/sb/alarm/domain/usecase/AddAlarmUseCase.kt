package com.sb.alarm.domain.usecase

import com.sb.alarm.domain.model.Alarm
import com.sb.alarm.domain.repository.AlarmRepository
import com.sb.alarm.domain.repository.AlarmSchedulerRepository
import com.sb.alarm.shared.constants.RepeatType
import javax.inject.Inject

class AddAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val alarmSchedulerRepository: AlarmSchedulerRepository,
) {
    /**
     * 알람 추가
     * @param medicationName 약 이름 또는 알람 라벨
     * @param hour 알람 시간 (0-23)
     * @param minute 알람 분 (0-59)
     * @param repeatType 반복 타입
     * @param repeatInterval 반복 간격 (기본값: 1)
     * @param repeatDaysOfWeek 매주 반복시 요일 목록 (일=0, 월=1, ..., 토=6)
     * @param startDate 알람 시작일 (밀리초 타임스탬프, null이면 현재 시간)
     * @param endDate 알람 종료일 (밀리초 타임스탬프, null이면 무기한)
     * @param isActive 알람 활성화 여부 (기본값: true)
     * @return 생성된 알람의 ID, 중복 알람이 있는 경우 -1L 반환
     */
    suspend operator fun invoke(
        medicationName: String,
        hour: Int,
        minute: Int,
        repeatType: RepeatType,
        repeatInterval: Int = 1,
        repeatDaysOfWeek: List<Int>? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        isActive: Boolean = true,
    ): Long {
        // 중복 알람 검사
        val hasDuplicate = alarmRepository.hasDuplicateAlarm(
            hour = hour,
            minute = minute,
            repeatType = repeatType,
            repeatInterval = repeatInterval,
            repeatDaysOfWeek = repeatDaysOfWeek
        )

        if (hasDuplicate) {
            return -1L // 중복 알람이 있음을 나타내는 값
        }

        val alarm = Alarm(
            medicationName = medicationName,
            hour = hour,
            minute = minute,
            repeatType = repeatType,
            repeatInterval = repeatInterval,
            repeatDaysOfWeek = repeatDaysOfWeek,
            startDate = startDate ?: System.currentTimeMillis(),
            endDate = endDate,
            isActive = isActive
        )

        // 1. 데이터베이스에 알람 저장
        val alarmId = alarmRepository.addAlarm(alarm)

        // 2. 성공적으로 저장되고 활성화된 알람인 경우 AlarmManager에도 등록
        if (alarmId > 0 && isActive) {
            val savedAlarm = alarm.copy(id = alarmId.toInt())
            alarmSchedulerRepository.schedule(savedAlarm)
        }

        return alarmId
    }
} 