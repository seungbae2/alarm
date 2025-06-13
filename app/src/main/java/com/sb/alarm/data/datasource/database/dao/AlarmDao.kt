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

    @Query("SELECT * FROM alarms")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE isActive = 1")
    fun getActiveAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Int): AlarmEntity?

    @Query("UPDATE alarms SET isActive = :isActive WHERE id = :id")
    suspend fun updateAlarmActiveStatus(id: Int, isActive: Boolean)

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteAlarm(id: Int)

    @Query(
        """
        SELECT COUNT(*) FROM alarms 
        WHERE isActive = 1 
        AND hour = :hour 
        AND minute = :minute 
        AND repeatType = :repeatType 
        AND repeatInterval = :repeatInterval 
        AND (
            (repeatDaysOfWeek IS NULL AND :repeatDaysOfWeek IS NULL) OR 
            (repeatDaysOfWeek = :repeatDaysOfWeek)
        )
    """
    )
    suspend fun countDuplicateAlarms(
        hour: Int,
        minute: Int,
        repeatType: String,
        repeatInterval: Int,
        repeatDaysOfWeek: String?,
    ): Int
} 