package com.sb.alarm.presentation.updateSchedule

import com.sb.alarm.domain.model.Alarm

data class AlternatingStep(
    val times: List<AlarmTime>,
    val durationDays: Int,
)

data class AlarmTime(val hour: Int, val minute: Int)

sealed class UpdateScheduleUiState {
    data object Loading : UpdateScheduleUiState()
    data class Success(val alarm: Alarm) : UpdateScheduleUiState()
    data class Error(val message: String) : UpdateScheduleUiState()
    data object Updating : UpdateScheduleUiState()
}

sealed class UpdateScheduleEvent {
    data class LoadAlarm(val alarmId: Int) : UpdateScheduleEvent()

    data class UpdateAlarm(
        val hour: Int,
        val minute: Int,
        val startDate: String, // "2025-01-15" 형식
    ) : UpdateScheduleEvent()

    data class UpdateAlternatingAlarm(
        val alternatingSteps: List<AlternatingStep>,
        val startDate: String,
    ) : UpdateScheduleEvent()

    data object NavigateBack : UpdateScheduleEvent()
}

sealed class UpdateScheduleEffect {
    data class ShowToast(val message: String) : UpdateScheduleEffect()
    data object NavigateBack : UpdateScheduleEffect()
    data object UpdateSuccess : UpdateScheduleEffect()
}

