package com.sb.alarm.data.datasource.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sb.alarm.shared.RepeatType

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    var medicationName: String, // 약 이름 또는 알람 라벨

    // 1. 기본 알람 시간
    val hour: Int,              // 알람 시간 (0-23)
    val minute: Int,            // 알람 분 (0-59)
    
    // 2. 반복 주기 설정
    val repeatType: RepeatType, // 반복 타입 (매일, 매주, n일 간격 등)
    val repeatInterval: Int = 1,  // 반복 간격 (예: repeatType이 DAYS_INTERVAL이고 이 값이 3이면, 3일 간격)
    
    // repeatType이 WEEKLY일 경우 사용될 요일 목록 (예: [1, 3, 5] -> 월, 수, 금)
    // TypeConverter를 사용하여 List<Int>를 String이나 다른 형태로 변환하여 저장해야 함
    val repeatDaysOfWeek: List<Int>? = null, 

    // 3. 알람 기간 설정
    val startDate: Long,        // 주기 시작일 (Timestamp)
    val endDate: Long? = null,    // 주기 종료일 (Timestamp, null이면 무기한)

    var isActive: Boolean = true // 알람 전체의 활성화 여부
) 