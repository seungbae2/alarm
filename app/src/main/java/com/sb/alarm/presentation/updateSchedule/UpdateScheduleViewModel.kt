package com.sb.alarm.presentation.updateSchedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sb.alarm.domain.model.Alarm
import com.sb.alarm.domain.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateScheduleViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UpdateScheduleUiState>(UpdateScheduleUiState.Loading)
    val uiState: StateFlow<UpdateScheduleUiState> = _uiState.asStateFlow()

    private val _effect = Channel<UpdateScheduleEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: UpdateScheduleEvent) {
        when (event) {
            is UpdateScheduleEvent.LoadAlarm -> loadAlarm(event.alarmId)
            is UpdateScheduleEvent.UpdateAlarm -> updateAlarm(event.hour, event.minute)
            is UpdateScheduleEvent.NavigateBack -> navigateBack()
        }
    }

    private fun loadAlarm(alarmId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = UpdateScheduleUiState.Loading
                val alarm = alarmRepository.getAlarmById(alarmId)
                if (alarm != null) {
                    _uiState.value = UpdateScheduleUiState.Success(alarm)
                } else {
                    _uiState.value = UpdateScheduleUiState.Error("알람을 찾을 수 없습니다.")
                }
            } catch (e: Exception) {
                _uiState.value = UpdateScheduleUiState.Error("알람 로드 중 오류가 발생했습니다: ${e.message}")
            }
        }
    }

    private fun updateAlarm(hour: Int, minute: Int) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is UpdateScheduleUiState.Success) {
                    val updatedAlarm = currentState.alarm.copy(
                        hour = hour,
                        minute = minute
                    )
                    alarmRepository.updateAlarm(updatedAlarm)
                    _effect.send(UpdateScheduleEffect.ShowToast("알람이 성공적으로 수정되었습니다."))
                    _effect.send(UpdateScheduleEffect.UpdateSuccess)
                } else {
                    _effect.send(UpdateScheduleEffect.ShowToast("알람 정보를 불러올 수 없습니다."))
                }
            } catch (e: Exception) {
                _effect.send(UpdateScheduleEffect.ShowToast("알람 수정 중 오류가 발생했습니다: ${e.message}"))
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effect.send(UpdateScheduleEffect.NavigateBack)
        }
    }
}