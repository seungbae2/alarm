package com.sb.alarm.presentation.alarm

import androidx.compose.runtime.Stable
import com.sb.alarm.domain.model.Alarm

@Stable
sealed class AlarmUiState {
    data object Loading : AlarmUiState()
    data class Success(
        val alarm: Alarm,
        val currentTime: String,
        val currentDate: String,
    ) : AlarmUiState()

    data object Error : AlarmUiState()
}

sealed interface AlarmUiEvent {
    data object TakeCompleted : AlarmUiEvent
    data object Dismiss : AlarmUiEvent
}

sealed interface AlarmEffect {
    data object NavigateToSchedule : AlarmEffect
    data object NavigateToScheduleAfterDismiss : AlarmEffect
} 