package com.sb.alarm.domain.repository

import com.sb.alarm.domain.model.Alarm
import com.sb.alarm.domain.model.AlarmHistory
import com.sb.alarm.shared.RepeatType
import com.sb.alarm.shared.TakeStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface AlarmRepository {

    /** 특정 날짜 범위의 활성 알람 조회 (기본 필터링) */
    fun getActiveAlarmsForDateRange(targetDate: LocalDate): Flow<List<Alarm>>

    /** 특정 알람 조회 */
    suspend fun getAlarmById(id: Int): Alarm?

    /** 알람 추가 */
    suspend fun addAlarm(alarm: Alarm): Long
    
    /** 알람 활성화 상태 업데이트 */
    suspend fun updateAlarmActiveStatus(id: Int, isActive: Boolean)
    
    /** 중복 알람 검사 */
    suspend fun hasDuplicateAlarm(
        hour: Int,
        minute: Int,
        repeatType: RepeatType,
        repeatInterval: Int,
        repeatDaysOfWeek: List<Int>?
    ): Boolean

    /** 특정 날짜의 알람 히스토리 조회 */
    fun getHistoryByDate(date: String): Flow<List<AlarmHistory>>

    /** 알람 히스토리 저장 */
    suspend fun saveAlarmHistory(history: AlarmHistory)

    /** 알람 상태 업데이트 */
    suspend fun updateAlarmStatus(alarmId: Int, date: String, status: TakeStatus)
}