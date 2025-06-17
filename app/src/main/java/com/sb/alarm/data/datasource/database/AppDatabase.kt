package com.sb.alarm.data.datasource.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sb.alarm.data.datasource.database.converter.DatabaseConverters
import com.sb.alarm.data.datasource.database.dao.AlarmDao
import com.sb.alarm.data.datasource.database.dao.AlarmHistoryDao
import com.sb.alarm.data.datasource.database.entity.AlarmEntity
import com.sb.alarm.data.datasource.database.entity.AlarmHistoryEntity

@Database(
    entities = [
        AlarmEntity::class,
        AlarmHistoryEntity::class
    ],
    version = 7, // 1분뒤 알람 히스토리 필드 추가로 버전 업그레이드
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun alarmHistoryDao(): AlarmHistoryDao
}