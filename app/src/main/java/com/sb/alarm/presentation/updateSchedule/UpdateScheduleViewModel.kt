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
import java.time.LocalDate
import java.time.ZoneId
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
            is UpdateScheduleEvent.UpdateAlarm -> updateAlarm(
                event.hour,
                event.minute,
                event.startDate
            )

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

    private fun updateAlarm(hour: Int, minute: Int, startDate: String) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is UpdateScheduleUiState.Success) {
                    val originalAlarm = currentState.alarm

                    // 시작 날짜를 LocalDate로 파싱하고 타임스탬프로 변환
                    val startLocalDate = LocalDate.parse(startDate)
                    val startTimestamp =
                        startLocalDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000

                    // 이전 날짜 계산 (시작 날짜 하루 전)
                    val endLocalDate = startLocalDate.minusDays(1)
                    val endTimestamp =
                        endLocalDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault())
                            .toEpochSecond() * 1000

                    // 1. 기존 알람의 종료 날짜를 설정하여 변경 전 날짜까지만 유효하게 함
                    val updatedOriginalAlarm = originalAlarm.copy(endDate = endTimestamp)
                    alarmRepository.updateAlarm(updatedOriginalAlarm)

                    // 2. 새로운 알람을 생성 (선택한 날짜부터 새로운 시간으로 시작)
                    val newAlarm = originalAlarm.copy(
                        id = 0, // 새 알람이므로 ID 초기화
                        hour = hour,
                        minute = minute,
                        startDate = startTimestamp,
                        endDate = null // 새 알람은 종료 날짜 없음 (무기한)
                    )
                    alarmRepository.addAlarm(newAlarm)

                    _effect.send(UpdateScheduleEffect.ShowToast("${startDate}부터 알람이 새로운 시간으로 변경되었습니다."))
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