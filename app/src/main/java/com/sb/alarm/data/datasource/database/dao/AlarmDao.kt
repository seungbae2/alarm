package com.sb.alarm.data.datasource.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sb.alarm.data.datasource.database.entity.AlarmEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    
    @Insert
    suspend fun insert(alarm: AlarmEntity): Long
    
    @Update
    suspend fun update(alarm: AlarmEntity)
    
    @Query("SELECT * FROM alarms WHERE isActive = 1")
    fun getAllActiveAlarms(): Flow<List<AlarmEntity>>
    
    @Query("SELECT * FROM alarms")
    fun getAllAlarms(): Flow<List<AlarmEntity>>
    
    @Query("""
        SELECT * FROM alarms 
        WHERE isActive = 1 
        AND (endDate IS NULL OR endDate >= :targetDateTimestamp)
        AND startDate <= :targetDateTimestamp
    """)
    fun getActiveAlarmsForDateRange(targetDateTimestamp: Long): Flow<List<AlarmEntity>>
    
    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Int): AlarmEntity?
    
    @Query("UPDATE alarms SET isActive = :isActive WHERE id = :id")
    suspend fun updateAlarmActiveStatus(id: Int, isActive: Boolean)
    
    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteAlarm(id: Int)
} 