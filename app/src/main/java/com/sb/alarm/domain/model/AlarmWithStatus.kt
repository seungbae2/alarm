package com.sb.alarm.domain.model

import com.sb.alarm.shared.constants.TakeStatus

/**
 * 알람 정보와 특정 날짜의 복용 상태를 함께 담는 데이터 클래스
 */
data class AlarmWithStatus(
    val alarm: Alarm,
    val takeStatus: TakeStatus = TakeStatus.NOT_ACTION, // 기본값은 아직 조치하지 않음
    val actionTimestamp: Long? = null, // 복용/스킵 처리한 시간
    
    // 1분뒤 알람 관련 정보
    val oneMinuteLaterTime: String? = null, // "14:35" 형식
    val oneMinuteLaterScheduledAt: Long? = null, // 1분뒤 알람이 설정된 시간
    val isOneMinuteLaterAlarm: Boolean = false, // 1분뒤 알람으로 인한 히스토리인지 여부
) 