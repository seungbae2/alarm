package com.sb.alarm.presentation.updateSchedule

import com.sb.alarm.domain.model.Alarm

data class UpdateScheduleUiState(
    val alarm: Alarm? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class UpdateScheduleEvent {
    data class LoadAlarm(val alarmId: Int) : UpdateScheduleEvent()
    data object NavigateBack : UpdateScheduleEvent()
}

sealed class UpdateScheduleEffect {
    data class ShowToast(val message: String) : UpdateScheduleEffect()
    data object NavigateBack : UpdateScheduleEffect()
}

