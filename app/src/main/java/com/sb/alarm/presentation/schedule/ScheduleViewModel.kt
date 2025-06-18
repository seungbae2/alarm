package com.sb.alarm.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sb.alarm.domain.model.AlarmWithStatus
import com.sb.alarm.domain.repository.AlarmRepository
import com.sb.alarm.domain.repository.AlarmSchedulerRepository
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
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val addAlarmUseCase: AddAlarmUseCase,
    private val getAlarmsByDateUseCase: GetAlarmsByDateUseCase,
    private val alarmRepository: AlarmRepository,
    private val alarmSchedulerRepository: AlarmSchedulerRepository,
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

    private fun setAlarmInOneMinute(alarmWithStatus: AlarmWithStatus) {
        viewModelScope.launch {
            try {
                // 1. 현재 시간 + 1분 계산
                val currentTime = Clock.System.now()
                val oneMinuteLater = currentTime.plus(1, DateTimeUnit.MINUTE)
                val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val oneMinuteLaterTimeString = "${
                    oneMinuteLater.toLocalDateTime(TimeZone.currentSystemDefault()).hour.toString()
                        .padStart(2, '0')
                }:${
                    oneMinuteLater.toLocalDateTime(TimeZone.currentSystemDefault()).minute.toString()
                        .padStart(2, '0')
                }"

                // 2. 히스토리에 1분뒤 알람 정보 저장
                alarmRepository.saveOneMinuteLaterHistory(
                    alarmId = alarmWithStatus.alarm.id,
                    date = currentDate.toString(),
                    oneMinuteLaterTime = oneMinuteLaterTimeString
                )

                // 3. AlarmManager에 1분 후 알람 등록
                alarmSchedulerRepository.scheduleOneTimeAlarm(alarmWithStatus.alarm)

                // 4. UI 즉시 업데이트 (현재 선택된 날짜 새로고침)
//                loadAlarmsForDate(currentDate)

                // 5. 성공 메시지 표시
                _effect.send(ScheduleEffect.ShowToast("1분 후 ${oneMinuteLaterTimeString}에 알람이 울립니다"))

            } catch (e: Exception) {
                _effect.send(ScheduleEffect.ShowToast("알람 설정에 실패했습니다: ${e.message}"))
            }
        }
    }

    private fun updateSchedule(alarmWithStatus: AlarmWithStatus, selectedDate: LocalDate) {
        viewModelScope.launch {
            _effect.send(ScheduleEffect.NavigateToEditSchedule(alarmWithStatus, selectedDate))
        }
    }
} 