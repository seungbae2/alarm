package com.sb.alarm.data.repository

import com.sb.alarm.data.datasource.database.dao.AlarmDao
import com.sb.alarm.data.datasource.database.dao.AlarmHistoryDao
import com.sb.alarm.data.datasource.database.mapper.toDomainModel
import com.sb.alarm.data.datasource.database.mapper.toEntity
import com.sb.alarm.domain.model.Alarm
import com.sb.alarm.domain.model.AlarmHistory
import com.sb.alarm.domain.repository.AlarmRepository
import com.sb.alarm.shared.constants.RepeatType
import com.sb.alarm.shared.constants.TakeStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    private val alarmDao: AlarmDao,
    private val alarmHistoryDao: AlarmHistoryDao,
) : AlarmRepository {

    override fun getActiveAlarms(): Flow<List<Alarm>> {
        return alarmDao.getActiveAlarms().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getAlarmById(id: Int): Alarm? {
        return alarmDao.getAlarmById(id)?.toDomainModel()
    }

    override suspend fun addAlarm(alarm: Alarm): Long {
        return alarmDao.insert(alarm.toEntity())
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        return alarmDao.update(alarm.toEntity())
    }

    override suspend fun updateAlarmActiveStatus(id: Int, isActive: Boolean) {
        alarmDao.updateAlarmActiveStatus(id, isActive)
    }

    override suspend fun hasDuplicateAlarm(
        hour: Int,
        minute: Int,
        repeatType: RepeatType,
        repeatInterval: Int,
        repeatDaysOfWeek: List<Int>?,
    ): Boolean {
        val repeatDaysOfWeekJson = repeatDaysOfWeek?.let { Json.encodeToString(it) }
        val duplicateCount = alarmDao.countDuplicateAlarms(
            hour = hour,
            minute = minute,
            repeatType = repeatType.name,
            repeatInterval = repeatInterval,
            repeatDaysOfWeek = repeatDaysOfWeekJson
        )
        return duplicateCount > 0
    }

    override fun getHistoryByDate(date: String): Flow<List<AlarmHistory>> {
        return alarmHistoryDao.getHistoryByDate(date).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun saveAlarmHistory(history: AlarmHistory) {
        alarmHistoryDao.insert(history.toEntity())
    }

    override suspend fun updateAlarmStatus(alarmId: Int, date: String, status: TakeStatus) {
        alarmHistoryDao.updateStatus(alarmId, date, status, System.currentTimeMillis())
    }

    override suspend fun saveOneMinuteLaterHistory(
        alarmId: Int,
        date: String,
        oneMinuteLaterTime: String,
    ) {
        // 기존 히스토리가 있는지 확인
        val existingHistory = alarmHistoryDao.getHistoryByAlarmAndDate(alarmId, date)

        if (existingHistory != null) {
            // 기존 히스토리가 있으면 1분뒤 알람 정보만 업데이트
            alarmHistoryDao.updateOneMinuteLaterInfo(
                alarmId,
                date,
                oneMinuteLaterTime,
                System.currentTimeMillis()
            )
        } else {
            // 기존 히스토리가 없으면 새로 생성
            val newHistory = AlarmHistory(
                alarmId = alarmId,
                logDate = date,
                status = TakeStatus.NOT_ACTION, // 기본 상태: 아직 조치하지 않음
                actionTimestamp = 0, // 아직 조치하지 않음
                oneMinuteLaterTime = oneMinuteLaterTime,
                oneMinuteLaterScheduledAt = System.currentTimeMillis()
            )
            saveAlarmHistory(newHistory)
        }
    }
} 