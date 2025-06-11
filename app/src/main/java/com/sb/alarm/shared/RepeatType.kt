package com.sb.alarm.shared

/**
 * 반복 타입을 정의하는 Enum 클래스
 * 이 Enum은 TypeConverter를 통해 DB에 String으로 저장됩니다.
 */
enum class RepeatType {
    NONE,           // 반복 없음 (한 번만 울림)
    DAILY,          // 매일
    DAYS_INTERVAL,  // N일 간격 (예: 격일, 3일마다)
    WEEKLY          // 매주 특정 요일
    // 추후 MONTHLY (매월 특정일) 등 확장 가능
} 