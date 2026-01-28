package com.sb.alarm.domain.usecase

import com.sb.alarm.domain.model.AlarmHistory
import com.sb.alarm.domain.repository.AlarmRepository
import com.sb.alarm.shared.constants.TakeStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class SaveAlarmHistoryUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository,
) {
    suspend operator fun invoke(
        alarmId: Int,
        status: TakeStatus,
        isOneMinuteLaterAlarm: Boolean = false
    ) {
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date.toString()

        val history = AlarmHistory(
            alarmId = alarmId,
            logDate = today,
            status = status,
            actionTimestamp = System.currentTimeMillis(),
            isOneMinuteLaterAlarm = isOneMinuteLaterAlarm
        )
        alarmRepository.saveAlarmHistory(history)
    }
}
