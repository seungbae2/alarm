package com.sb.alarm.domain.repository

import com.sb.alarm.domain.model.Alarm
import com.sb.alarm.shared.RepeatType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface AlarmRepository {

    /** 특정 날짜 범위의 활성 알람 조회 (기본 필터링) */
    fun getActiveAlarmsForDateRange(targetDate: LocalDate): Flow<List<Alarm>>

    /** 특정 알람 조회 */
    suspend fun getAlarmById(id: Int): Alarm?

    /** 알람 추가 */
    suspend fun addAlarm(alarm: Alarm): Long
    
    /** 중복 알람 검사 */
    suspend fun hasDuplicateAlarm(
        hour: Int,
        minute: Int,
        repeatType: RepeatType,
        repeatInterval: Int,
        repeatDaysOfWeek: List<Int>?
    ): Boolean
}