package com.sb.alarm.di

import android.content.Context
import androidx.room.Room
import com.sb.alarm.data.datasource.database.AppDatabase
import com.sb.alarm.data.datasource.database.migration.MIGRATION_6_7
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "alarm_database"
        )
        .addMigrations(MIGRATION_6_7)  // 6에서 7로 마이그레이션 추가
        .build()
    }
}