package com.sb.alarm.di

import com.sb.alarm.data.datasource.database.AppDatabase
import com.sb.alarm.data.datasource.database.dao.AlarmDao
import com.sb.alarm.data.datasource.database.dao.AlarmHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    
    @Provides
    fun provideAlarmDao(database: AppDatabase): AlarmDao {
        return database.alarmDao()
    }
    
    @Provides
    fun provideAlarmHistoryDao(database: AppDatabase): AlarmHistoryDao {
        return database.alarmHistoryDao()
    }
}