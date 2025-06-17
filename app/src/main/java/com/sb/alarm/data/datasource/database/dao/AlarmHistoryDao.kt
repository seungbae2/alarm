package com.sb.alarm.data.datasource.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sb.alarm.data.datasource.database.entity.AlarmHistoryEntity
import com.sb.alarm.shared.constants.TakeStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmHistoryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(history: AlarmHistoryEntity): Long

    @Update
    suspend fun update(history: AlarmHistoryEntity)

    @Query("SELECT * FROM alarm_history WHERE alarmId = :alarmId AND logDate = :logDate")
    suspend fun getHistory(alarmId: Int, logDate: String): AlarmHistoryEntity?

    @Query("SELECT * FROM alarm_history WHERE alarmId = :alarmId")
    fun getHistoryByAlarmId(alarmId: Int): Flow<List<AlarmHistoryEntity>>

    @Query("SELECT * FROM alarm_history WHERE logDate = :logDate")
    fun getHistoryByDate(logDate: String): Flow<List<AlarmHistoryEntity>>

    @Query("UPDATE alarm_history SET status = :status, actionTimestamp = :actionTimestamp WHERE alarmId = :alarmId AND logDate = :logDate")
    suspend fun updateStatus(
        alarmId: Int,
        logDate: String,
        status: TakeStatus,
        actionTimestamp: Long,
    )

    @Query("DELETE FROM alarm_history WHERE alarmId = :alarmId")
    suspend fun deleteHistoryByAlarmId(alarmId: Int)

    /** 1분뒤 알람 관련 메서드들 */
    @Query("SELECT * FROM alarm_history WHERE alarmId = :alarmId AND logDate = :logDate")
    suspend fun getHistoryByAlarmAndDate(alarmId: Int, logDate: String): AlarmHistoryEntity?

    @Query("UPDATE alarm_history SET oneMinuteLaterTime = :oneMinuteLaterTime, oneMinuteLaterScheduledAt = :scheduledAt WHERE alarmId = :alarmId AND logDate = :logDate")
    suspend fun updateOneMinuteLaterInfo(
        alarmId: Int,
        logDate: String,
        oneMinuteLaterTime: String,
        scheduledAt: Long,
    )
} 