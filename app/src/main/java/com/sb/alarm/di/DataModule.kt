package com.sb.alarm.di

import com.sb.alarm.data.repository.AlarmRepositoryImpl
import com.sb.alarm.data.repository.AlarmSchedulerRepositoryImpl
import com.sb.alarm.domain.repository.AlarmRepository
import com.sb.alarm.domain.repository.AlarmSchedulerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindAlarmRepository(
        alarmRepositoryImpl: AlarmRepositoryImpl,
    ): AlarmRepository

    @Binds
    abstract fun bindAlarmSchedulerRepository(
        alarmSchedulerRepositoryImpl: AlarmSchedulerRepositoryImpl,
    ): AlarmSchedulerRepository
}