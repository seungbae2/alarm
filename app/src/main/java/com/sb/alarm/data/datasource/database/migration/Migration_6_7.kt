package com.sb.alarm.data.datasource.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // AlarmHistory 테이블에 1분뒤 알람 관련 필드 추가
        database.execSQL(
            """
            ALTER TABLE alarm_history 
            ADD COLUMN oneMinuteLaterTime TEXT DEFAULT NULL
            """.trimIndent()
        )
        
        database.execSQL(
            """
            ALTER TABLE alarm_history 
            ADD COLUMN oneMinuteLaterScheduledAt INTEGER DEFAULT NULL
            """.trimIndent()
        )
    }
} 