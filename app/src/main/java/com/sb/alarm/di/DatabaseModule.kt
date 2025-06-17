package com.sb.alarm.di

import android.content.Context
import androidx.room.Room
import com.sb.alarm.data.datasource.database.AppDatabase
import com.sb.alarm.data.datasource.database.migration.MIGRATION_6_7
import com.sb.alarm.data.datasource.database.migration.MIGRATION_7_8
import com.sb.alarm.data.datasource.database.migration.MIGRATION_8_9
import com.sb.alarm.data.datasource.database.migration.MIGRATION_9_10
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
        .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)  // 마이그레이션 추가: 6→7, 7→8, 8→9, 9→10
        .build()
    }
}