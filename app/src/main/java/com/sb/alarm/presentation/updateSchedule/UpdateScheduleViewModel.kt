package com.sb.alarm.presentation.updateSchedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sb.alarm.domain.usecase.GetAlarmByIdUseCase
import com.sb.alarm.domain.usecase.UpdateAlternatingAlarmUseCase
import com.sb.alarm.domain.usecase.UpdateDailyAlarmFromDateUseCase
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
    private val getAlarmByIdUseCase: GetAlarmByIdUseCase,
    private val updateDailyAlarmFromDateUseCase: UpdateDailyAlarmFromDateUseCase,
    private val updateAlternatingAlarmUseCase: UpdateAlternatingAlarmUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UpdateScheduleUiState>(UpdateScheduleUiState.Loading)
    val uiState: StateFlow<UpdateScheduleUiState> = _uiState.asStateFlow()

    private val _effect = Channel<UpdateScheduleEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: UpdateScheduleEvent) {
        when (event) {
            is UpdateScheduleEvent.LoadAlarm -> loadAlarm(event.alarmId)
            is UpdateScheduleEvent.UpdateAlarm -> updateAlarm(
                event.hour,
                event.minute,
                event.startDate
            )

            is UpdateScheduleEvent.UpdateAlternatingAlarm -> updateAlternatingAlarm(
                event.alternatingSteps,
                event.startDate
            )

            is UpdateScheduleEvent.NavigateBack -> navigateBack()
        }
    }

    private fun loadAlarm(alarmId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = UpdateScheduleUiState.Loading
                val alarm = getAlarmByIdUseCase(alarmId)
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

    private fun updateAlarm(hour: Int, minute: Int, startDate: String) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is UpdateScheduleUiState.Success) {
                    _uiState.value = UpdateScheduleUiState.Updating
                    val originalAlarm = currentState.alarm

                    val result = updateDailyAlarmFromDateUseCase(
                        originalAlarm = originalAlarm,
                        hour = hour,
                        minute = minute,
                        startDate = startDate
                    )

                    result.onSuccess {
                        _effect.send(UpdateScheduleEffect.ShowToast("${startDate}부터 알람이 새로운 시간으로 변경되었습니다."))
                        _effect.send(UpdateScheduleEffect.UpdateSuccess)
                    }.onFailure { error ->
                        _uiState.value =
                            UpdateScheduleUiState.Error("알람 수정 중 오류가 발생했습니다: ${error.message}")
                        _effect.send(UpdateScheduleEffect.ShowToast("알람 수정 중 오류가 발생했습니다: ${error.message}"))
                    }
                } else {
                    _uiState.value = UpdateScheduleUiState.Error("알람 정보를 불러올 수 없습니다.")
                    _effect.send(UpdateScheduleEffect.ShowToast("알람 정보를 불러올 수 없습니다."))
                }
            } catch (e: Exception) {
                _uiState.value = UpdateScheduleUiState.Error("알람 수정 중 오류가 발생했습니다: ${e.message}")
                _effect.send(UpdateScheduleEffect.ShowToast("알람 수정 중 오류가 발생했습니다: ${e.message}"))
            }
        }
    }

    private fun updateAlternatingAlarm(alternatingSteps: List<AlternatingStep>, startDate: String) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is UpdateScheduleUiState.Success) {
                    _uiState.value = UpdateScheduleUiState.Updating
                    val originalAlarm = currentState.alarm

                    val result = updateAlternatingAlarmUseCase(
                        originalAlarm = originalAlarm,
                        alternatingSteps = alternatingSteps,
                        startDate = startDate
                    )

                    result.onSuccess {
                        _effect.send(UpdateScheduleEffect.ShowToast("교대형 알람이 설정되었습니다."))
                        _effect.send(UpdateScheduleEffect.UpdateSuccess)
                    }.onFailure { error ->
                        _uiState.value =
                            UpdateScheduleUiState.Error("알람 설정 중 오류가 발생했습니다: ${error.message}")
                        _effect.send(UpdateScheduleEffect.ShowToast("알람 설정 중 오류가 발생했습니다: ${error.message}"))
                    }
                } else {
                    _uiState.value = UpdateScheduleUiState.Error("알람 정보를 불러올 수 없습니다.")
                    _effect.send(UpdateScheduleEffect.ShowToast("알람 정보를 불러올 수 없습니다."))
                }
            } catch (e: Exception) {
                _uiState.value = UpdateScheduleUiState.Error("알람 설정 중 오류가 발생했습니다: ${e.message}")
                _effect.send(UpdateScheduleEffect.ShowToast("알람 설정 중 오류가 발생했습니다: ${e.message}"))
            }
        }
    }


    private fun navigateBack() {
        viewModelScope.launch {
            _effect.send(UpdateScheduleEffect.NavigateBack)
        }
    }
}