package com.sb.alarm.shared.constants

// 복용 상태를 나타내는 Enum 클래스 (TypeConverter를 사용하여 Room에 저장)
enum class TakeStatus {
    TAKEN,  // 약 먹음
    SKIPPED // 약 스킵
} 