package com.sb.alarm.presentation.updateSchedule

import com.sb.alarm.domain.model.Alarm
import com.sb.alarm.domain.model.AlternatingStep

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

