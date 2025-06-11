package com.sb.alarm.data.repository

import com.sb.alarm.data.datasource.database.dao.AlarmDao
import com.sb.alarm.data.datasource.database.dao.AlarmHistoryDao
import com.sb.alarm.data.datasource.database.mapper.toDomainModel
import com.sb.alarm.data.datasource.database.mapper.toEntity
import com.sb.alarm.domain.model.Alarm
import com.sb.alarm.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    private val alarmDao: AlarmDao,
    private val alarmHistoryDao: AlarmHistoryDao,
) : AlarmRepository {

    override fun getActiveAlarmsForDateRange(targetDate: LocalDate): Flow<List<Alarm>> {
        val targetDateTimestamp =
            targetDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        return alarmDao.getActiveAlarmsForDateRange(targetDateTimestamp).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getAlarmById(id: Int): Alarm? {
        return alarmDao.getAlarmById(id)?.toDomainModel()
    }

    override suspend fun addAlarm(alarm: Alarm): Long {
        return alarmDao.insert(alarm.toEntity())
    }
} 