package com.sb.alarm.data.datasource.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // alarm_history 테이블에 isOneMinuteLaterAlarm 컬럼 추가
        database.execSQL("""
            ALTER TABLE alarm_history 
            ADD COLUMN isOneMinuteLaterAlarm INTEGER NOT NULL DEFAULT 0
        """)
    }
}
