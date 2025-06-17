package com.sb.alarm.presentation.updateSchedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _uiState = MutableStateFlow(UpdateScheduleUiState())
    val uiState: StateFlow<UpdateScheduleUiState> = _uiState.asStateFlow()

    private val _effect = Channel<UpdateScheduleEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: UpdateScheduleEvent) {
        when (event) {
            is UpdateScheduleEvent.LoadAlarm -> loadAlarm(event.alarmId)
            is UpdateScheduleEvent.NavigateBack -> navigateBack()
        }
    }

    private fun loadAlarm(alarmId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val alarm = alarmRepository.getAlarmById(alarmId)
                if (alarm != null) {
                    _uiState.value = _uiState.value.copy(
                        alarm = alarm,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "알람을 찾을 수 없습니다.",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "알람 로드 중 오류가 발생했습니다: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effect.send(UpdateScheduleEffect.NavigateBack)
        }
    }
}