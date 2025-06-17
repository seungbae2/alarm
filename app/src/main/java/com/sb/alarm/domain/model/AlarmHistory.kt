package com.sb.alarm.domain.model

import com.sb.alarm.shared.constants.TakeStatus

/**
 * 알람 복용 히스토리 도메인 모델
 */
data class AlarmHistory(
    val id: Long = 0, // auto-generated id
    val alarmId: Int,
    val logDate: String, // "2024-01-15" 형식
    val status: TakeStatus,
    val actionTimestamp: Long,
    
    // 1분뒤 알람 관련 필드
    val oneMinuteLaterTime: String? = null, // "14:35" 형식
    val oneMinuteLaterScheduledAt: Long? = null, // 1분뒤 알람이 설정된 시간
    val isOneMinuteLaterAlarm: Boolean = false, // 1분뒤 알람으로 인한 히스토리인지 여부
) 