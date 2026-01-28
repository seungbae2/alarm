package com.sb.alarm.domain.usecase

import com.sb.alarm.domain.model.Alarm
import com.sb.alarm.domain.repository.AlarmRepository
import com.sb.alarm.domain.repository.AlarmSchedulerRepository
import com.sb.alarm.shared.constants.RepeatType
import com.sb.alarm.shared.constants.TakeStatus
import javax.inject.Inject

class HandleAlarmCompletionUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val alarmSchedulerRepository: AlarmSchedulerRepository,
    private val saveAlarmHistoryUseCase: SaveAlarmHistoryUseCase,
) {
    suspend operator fun invoke(
        alarm: Alarm,
        alarmId: Int,
        status: TakeStatus,
        isOneMinuteLaterAlarm: Boolean = false
    ): Result<Unit> {
        return try {
            saveAlarmHistoryUseCase(alarmId, status, isOneMinuteLaterAlarm)

            when (alarm.repeatType) {
                RepeatType.NONE -> {
                    alarmRepository.updateAlarmActiveStatus(alarmId, false)
                }
                else -> {
                    alarmSchedulerRepository.schedule(alarm)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
