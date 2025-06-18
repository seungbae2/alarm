package com.sb.alarm.domain.usecase

import com.sb.alarm.domain.model.Alarm
import com.sb.alarm.domain.repository.AlarmRepository
import javax.inject.Inject

class GetAlarmByIdUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository,
) {
    /**
     * ID로 알람을 조회합니다.
     * 
     * @param alarmId 조회할 알람의 ID
     * @return 알람 정보 또는 null
     */
    suspend operator fun invoke(alarmId: Int): Alarm? {
        return alarmRepository.getAlarmById(alarmId)
    }
} 