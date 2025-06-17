package com.sb.alarm.data.datasource.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.sb.alarm.shared.constants.TakeStatus

@Entity(
    tableName = "alarm_history",
    // alarmId를 Alarm 테이블의 id와 연결하는 외래 키(Foreign Key) 설정
    foreignKeys = [
        ForeignKey(
            entity = AlarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["alarmId"],
            onDelete = ForeignKey.CASCADE // 원본 알람 삭제 시 관련 기록도 모두 삭제
        )
    ]
)
data class AlarmHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // auto-generated primary key로 진짜 히스토리 관리
    
    val alarmId: Int, // 어떤 알람에 대한 기록인지 (Alarm의 id와 연결)

    val logDate: String, // 기록 날짜 (예: "2025-06-10")

    val status: TakeStatus, // 복용 상태 ('약 먹음', '약 스킵')

    val actionTimestamp: Long, // 사용자가 선택한 시간 (Timestamp)
    
    // 1분뒤 알람 관련 필드
    val oneMinuteLaterTime: String? = null, // "14:35" 형식
    val oneMinuteLaterScheduledAt: Long? = null, // 1분뒤 알람이 설정된 시간
    val isOneMinuteLaterAlarm: Boolean = false, // 1분뒤 알람으로 인한 히스토리인지 여부
) 