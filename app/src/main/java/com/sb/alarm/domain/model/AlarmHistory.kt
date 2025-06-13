package com.sb.alarm.domain.model

import com.sb.alarm.shared.TakeStatus

/**
 * 알람 복용 히스토리 도메인 모델
 */
data class AlarmHistory(
    val alarmId: Int,
    val logDate: String, // "2024-01-15" 형식
    val status: TakeStatus,
    val actionTimestamp: Long
) 