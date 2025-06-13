package com.sb.alarm.domain.usecase

import com.sb.alarm.domain.repository.AlarmRepository
import com.sb.alarm.shared.TakeStatus
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class UpdateAlarmStatusUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository,
) {
    /**
     * 특정 날짜의 알람 복용 상태를 업데이트합니다.
     * @param alarmId 알람 ID
     * @param date 날짜
     * @param status 복용 상태 (TAKEN 또는 SKIPPED)
     */
    suspend operator fun invoke(alarmId: Int, date: LocalDate, status: TakeStatus): Boolean {
        return try {
            val dateString = date.toString() // "2024-01-15" 형식
            alarmRepository.updateAlarmStatus(alarmId, dateString, status)
            true
        } catch (e: Exception) {
            false
        }
    }
} 