package com.sb.alarm.domain.model

import com.sb.alarm.shared.RepeatType

data class Alarm(
    val id: Int = 0,
    val medicationName: String,
    val hour: Int,
    val minute: Int,
    val repeatType: RepeatType,
    val repeatInterval: Int = 1,
    val repeatDaysOfWeek: List<Int>? = null,
    val startDate: Long,
    val endDate: Long? = null,
    val isActive: Boolean = true
) 