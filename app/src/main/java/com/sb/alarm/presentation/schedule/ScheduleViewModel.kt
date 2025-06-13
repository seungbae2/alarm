package com.sb.alarm.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sb.alarm.domain.usecase.AddAlarmUseCase
import com.sb.alarm.domain.usecase.GetAlarmsByDateUseCase
import com.sb.alarm.shared.constants.RepeatType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val addAlarmUseCase: AddAlarmUseCase,
    private val getAlarmsByDateUseCase: GetAlarmsByDateUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Loading)
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ScheduleEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: ScheduleEvent) {
        when (event) {
            is ScheduleEvent.AddAlarm -> addAlarm()
            is ScheduleEvent.LoadAlarms -> loadAlarmsForDate(date = event.date)
        }
    }

    fun loadAlarmsForDate(date: LocalDate) {
        viewModelScope.launch {
            // 선택된 날짜에 해당하는 알람과 상태 정보를 함께 가져오기
            getAlarmsByDateUseCase.invoke(date).collect { alarmWithStatusList ->
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

            if (result == -1L) {
                _effect.send(ScheduleEffect.ShowToast("동일한 알람이 이미 존재합니다."))
            } else {
                _effect.send(ScheduleEffect.ShowToast("알람이 성공적으로 추가되었습니다."))
            }
        }
    }

    fun addAlarm() {
        viewModelScope.launch {
            val result = addAlarmUseCase.invoke(
                medicationName = "테스트 알람",
                hour = 20,
                minute = 7,
                repeatType = RepeatType.DAILY,
                repeatInterval = 1,
                repeatDaysOfWeek = null, // 매일이므로 요일 지정 불필요
                startDate = null, // 현재 시간으로 설정
                endDate = null, // 무기한
                isActive = true
            )

            if (result == -1L) {
                _effect.send(ScheduleEffect.ShowToast("동일한 알람이 이미 존재합니다."))
            } else {
                _effect.send(ScheduleEffect.ShowToast("알람이 성공적으로 추가되었습니다."))
            }
        }
    }
} 