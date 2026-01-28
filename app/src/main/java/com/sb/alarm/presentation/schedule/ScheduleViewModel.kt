package com.sb.alarm.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sb.alarm.domain.model.AlarmWithStatus
import com.sb.alarm.domain.usecase.AddAlarmResult
import com.sb.alarm.domain.usecase.AddAlarmUseCase
import com.sb.alarm.domain.usecase.GetAlarmsByDateUseCase
import com.sb.alarm.domain.usecase.SetAlarmInOneMinuteUseCase
import com.sb.alarm.shared.constants.RepeatType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val addAlarmUseCase: AddAlarmUseCase,
    private val getAlarmsByDateUseCase: GetAlarmsByDateUseCase,
    private val setAlarmInOneMinuteUseCase: SetAlarmInOneMinuteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Loading)
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ScheduleEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: ScheduleEvent) {
        when (event) {
            is ScheduleEvent.AddAlarm -> addDailyNoonAlarm()
            is ScheduleEvent.LoadAlarms -> loadAlarmsForDate(date = event.date)
            is ScheduleEvent.SetAlarmInOneMinute -> {
                setAlarmInOneMinute(event.alarmWithStatus)
            }

            is ScheduleEvent.UpdateSchedule -> {
                updateSchedule(event.alarmWithStatus, event.selectedDate)
            }
        }
    }

    fun loadAlarmsForDate(date: LocalDate) {
        viewModelScope.launch {
            getAlarmsByDateUseCase.invoke(date)
                .catch { e ->
                    _uiState.value = ScheduleUiState.Error(e.message ?: "알람 로드 중 오류가 발생했습니다")
                }
                .collect { alarmWithStatusList ->
                    _uiState.value = ScheduleUiState.Success(alarmWithStatusList)
                }
        }
    }

    fun addDailyNoonAlarm() {
        viewModelScope.launch {
            val result = addAlarmUseCase.invoke(
                medicationName = "매일 점심 복용",
                hour = 12,
                minute = 0,
                repeatType = RepeatType.DAILY,
                repeatInterval = 1,
                repeatDaysOfWeek = null, // 매일이므로 요일 지정 불필요
                startDate = null, // 현재 시간으로 설정
                endDate = null, // 무기한
                isActive = true
            )

            when (result) {
                is AddAlarmResult.Success -> {
                    _effect.send(ScheduleEffect.ShowToast("알람이 성공적으로 추가되었습니다."))
                }
                is AddAlarmResult.DuplicateAlarm -> {
                    _effect.send(ScheduleEffect.ShowToast("동일한 알람이 이미 존재합니다."))
                }
                is AddAlarmResult.Error -> {
                    _effect.send(ScheduleEffect.ShowToast("알람 추가 중 오류가 발생했습니다: ${result.exception.message}"))
                }
            }
        }
    }

    private fun setAlarmInOneMinute(alarmWithStatus: AlarmWithStatus) {
        viewModelScope.launch {
            val result = setAlarmInOneMinuteUseCase(alarmWithStatus)
            
            result.onSuccess { oneMinuteLaterTime ->
                _effect.send(ScheduleEffect.ShowToast("1분 후 ${oneMinuteLaterTime}에 알람이 울립니다"))
            }.onFailure { error ->
                _effect.send(ScheduleEffect.ShowToast("알람 설정에 실패했습니다: ${error.message}"))
            }
        }
    }

    private fun updateSchedule(alarmWithStatus: AlarmWithStatus, selectedDate: LocalDate) {
        viewModelScope.launch {
            _effect.send(ScheduleEffect.NavigateToEditSchedule(alarmWithStatus, selectedDate))
        }
    }
} 