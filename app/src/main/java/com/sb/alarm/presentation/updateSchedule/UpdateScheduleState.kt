package com.sb.alarm.presentation.updateSchedule

import com.sb.alarm.domain.model.Alarm

sealed class UpdateScheduleUiState {
    data object Loading : UpdateScheduleUiState()
    data class Success(val alarm: Alarm) : UpdateScheduleUiState()
    data class Error(val message: String) : UpdateScheduleUiState()
}

sealed class UpdateScheduleEvent {
    data class LoadAlarm(val alarmId: Int) : UpdateScheduleEvent()
    data class UpdateAlarm(val hour: Int, val minute: Int) : UpdateScheduleEvent()
    data object NavigateBack : UpdateScheduleEvent()
}

sealed class UpdateScheduleEffect {
    data class ShowToast(val message: String) : UpdateScheduleEffect()
    data object NavigateBack : UpdateScheduleEffect()
    data object UpdateSuccess : UpdateScheduleEffect()
}

