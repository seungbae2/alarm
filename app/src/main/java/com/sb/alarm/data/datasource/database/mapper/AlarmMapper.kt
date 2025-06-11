package com.sb.alarm.data.datasource.database.mapper

import com.sb.alarm.data.datasource.database.entity.AlarmEntity
import com.sb.alarm.data.datasource.database.entity.AlarmHistoryEntity
import com.sb.alarm.domain.model.Alarm
import com.sb.alarm.shared.TakeStatus

fun AlarmEntity.toDomainModel(): Alarm {
    return Alarm(
        id = id,
        medicationName = medicationName,
        hour = hour,
        minute = minute,
        repeatType = repeatType,
        repeatInterval = repeatInterval,
        repeatDaysOfWeek = repeatDaysOfWeek,
        startDate = startDate,
        endDate = endDate,
        isActive = isActive
    )
}

fun Alarm.toEntity(): AlarmEntity {
    return AlarmEntity(
        id = id,
        medicationName = medicationName,
        hour = hour,
        minute = minute,
        repeatType = repeatType,
        repeatInterval = repeatInterval,
        repeatDaysOfWeek = repeatDaysOfWeek,
        startDate = startDate,
        endDate = endDate,
        isActive = isActive
    )
} 