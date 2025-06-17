package com.sb.alarm.domain.repository

import com.sb.alarm.domain.model.Alarm

interface AlarmSchedulerRepository {
    fun schedule(alarmItem: Alarm) // 비즈니스 로직에 필요한 데이터만 받음
    fun cancel(alarmId: Int)
    
    /** 1분뒤 일회성 알람 스케줄링 */
    fun scheduleOneTimeAlarm(alarmItem: Alarm)
}