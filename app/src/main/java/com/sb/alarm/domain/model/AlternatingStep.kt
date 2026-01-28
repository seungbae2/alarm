package com.sb.alarm.domain.model

data class AlternatingStep(
    val times: List<AlarmTime>,
    val durationDays: Int,
)

data class AlarmTime(val hour: Int, val minute: Int)
