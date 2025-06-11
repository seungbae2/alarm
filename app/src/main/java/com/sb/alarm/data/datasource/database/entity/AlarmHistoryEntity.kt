package com.sb.alarm.data.datasource.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import com.sb.alarm.shared.TakeStatus

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
    ],
    // 특정 날짜와 알람 ID의 조합은 유일해야 하므로 복합 기본 키로 설정
    primaryKeys = ["alarmId", "logDate"]
)
data class AlarmHistoryEntity(
    val alarmId: Int, // 어떤 알람에 대한 기록인지 (Alarm의 id와 연결)
    
    val logDate: String, // 기록 날짜 (예: "2025-06-10")

    val status: TakeStatus, // 복용 상태 ('약 먹음', '약 스킵')
    
    val actionTimestamp: Long // 사용자가 선택한 시간 (Timestamp)
) 