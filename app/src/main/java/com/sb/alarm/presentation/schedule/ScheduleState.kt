package com.sb.alarm.presentation.schedule

import com.sb.alarm.domain.model.AlarmWithStatus
import kotlinx.datetime.LocalDate


sealed class ScheduleUiState {
    data object Loading : ScheduleUiState()
    data class Success(val alarms: List<AlarmWithStatus>) : ScheduleUiState()
    data class Error(val message: String) : ScheduleUiState()
}

sealed class ScheduleEvent {
    data class LoadAlarms(val date: LocalDate) : ScheduleEvent()
    data object AddAlarm : ScheduleEvent()
}

sealed class ScheduleEffect {
    data class ShowToast(val message: String) : ScheduleEffect()
}