package com.sb.alarm.data.datasource.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Foreign Key 제약조건을 일시적으로 비활성화
        database.execSQL("PRAGMA foreign_keys=OFF")
        
        // 트랜잭션 시작
        database.beginTransaction()
        
        try {
            // 1. 기존 테이블 백업
            database.execSQL("""
                CREATE TABLE alarm_history_backup (
                    alarmId INTEGER NOT NULL,
                    logDate TEXT NOT NULL,
                    status TEXT NOT NULL,
                    actionTimestamp INTEGER NOT NULL,
                    oneMinuteLaterTime TEXT,
                    oneMinuteLaterScheduledAt INTEGER
                )
            """)
            
            // 2. 기존 데이터를 백업 테이블로 복사
            database.execSQL("""
                INSERT INTO alarm_history_backup 
                SELECT alarmId, logDate, status, actionTimestamp, oneMinuteLaterTime, oneMinuteLaterScheduledAt 
                FROM alarm_history
            """)
            
            // 3. 기존 테이블 삭제
            database.execSQL("DROP TABLE alarm_history")
            
            // 4. 새로운 구조로 테이블 재생성 (Room이 기대하는 정확한 스키마로)
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `alarm_history` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `alarmId` INTEGER NOT NULL, 
                    `logDate` TEXT NOT NULL, 
                    `status` TEXT NOT NULL, 
                    `actionTimestamp` INTEGER NOT NULL, 
                    `oneMinuteLaterTime` TEXT, 
                    `oneMinuteLaterScheduledAt` INTEGER, 
                    FOREIGN KEY(`alarmId`) REFERENCES `alarms`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """)
            
            // 5. 백업 데이터를 새 테이블로 복사 (id는 자동 생성)
            database.execSQL("""
                INSERT INTO alarm_history (alarmId, logDate, status, actionTimestamp, oneMinuteLaterTime, oneMinuteLaterScheduledAt)
                SELECT alarmId, logDate, status, actionTimestamp, oneMinuteLaterTime, oneMinuteLaterScheduledAt
                FROM alarm_history_backup
            """)
            
            // 6. 백업 테이블 삭제
            database.execSQL("DROP TABLE alarm_history_backup")
            
            // 트랜잭션 커밋
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
            // Foreign Key 제약조건 다시 활성화
            database.execSQL("PRAGMA foreign_keys=ON")
        }
    }
}
