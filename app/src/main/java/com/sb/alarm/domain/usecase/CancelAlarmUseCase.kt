package com.sb.alarm.domain.usecase

import com.sb.alarm.domain.repository.AlarmRepository
import com.sb.alarm.domain.repository.AlarmSchedulerRepository
import javax.inject.Inject

class CancelAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val alarmSchedulerRepository: AlarmSchedulerRepository,
) {
    /**
     * 알람 취소
     * @param alarmId 취소할 알람의 ID
     * @return 성공 여부
     */
    operator fun invoke(alarmId: Int): Boolean {
        return try {
            // 1. 데이터베이스에서 알람 비활성화
//            alarmRepository.updateAlarmActiveStatus(alarmId, false)

            // 2. AlarmManager에서 알람 취소
            alarmSchedulerRepository.cancel(alarmId)

            true
        } catch (e: Exception) {
            false
        }
    }
} 