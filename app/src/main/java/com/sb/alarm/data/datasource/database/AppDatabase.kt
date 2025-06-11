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
    version = 6,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun alarmHistoryDao(): AlarmHistoryDao
}