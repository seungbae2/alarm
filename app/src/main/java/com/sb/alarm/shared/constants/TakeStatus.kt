package com.sb.alarm.shared.constants

// 복용 상태를 나타내는 Enum 클래스 (TypeConverter를 사용하여 Room에 저장)
enum class TakeStatus {
    NOT_ACTION, // 아직 조치하지 않음
    TAKEN,      // 약 먹음
    SKIPPED     // 약 스킵
} 