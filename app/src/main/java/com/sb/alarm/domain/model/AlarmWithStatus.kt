package com.sb.alarm.domain.model

import com.sb.alarm.shared.constants.TakeStatus

/**
 * 알람 정보와 특정 날짜의 복용 상태를 함께 담는 데이터 클래스
 */
data class AlarmWithStatus(
    val alarm: Alarm,
    val takeStatus: TakeStatus? = null, // null이면 아직 복용하지 않음
    val actionTimestamp: Long? = null, // 복용/스킵 처리한 시간
) 