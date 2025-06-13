package com.sb.alarm.data.datasource.database.converter

import androidx.room.TypeConverter
import com.sb.alarm.shared.constants.RepeatType
import com.sb.alarm.shared.constants.TakeStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DatabaseConverters {

    @TypeConverter
    fun fromRepeatType(type: RepeatType): String = type.name

    @TypeConverter
    fun toRepeatType(value: String): RepeatType = RepeatType.valueOf(value)

    @TypeConverter
    fun fromTakeStatus(status: TakeStatus): String = status.name

    @TypeConverter
    fun toTakeStatus(value: String): TakeStatus = TakeStatus.valueOf(value)

    @TypeConverter
    fun fromIntList(value: List<Int>?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        return value?.let { Json.decodeFromString(it) }
    }
} 