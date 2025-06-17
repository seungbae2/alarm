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
    data class SetAlarmInOneMinute(val alarmWithStatus: AlarmWithStatus) : ScheduleEvent()
    data class EditSchedule(val alarmWithStatus: AlarmWithStatus) : ScheduleEvent()
}

sealed class ScheduleEffect {
    data class ShowToast(val message: String) : ScheduleEffect()
    data class NavigateToEditSchedule(val alarmWithStatus: AlarmWithStatus) : ScheduleEffect()
}