package com.sb.alarm.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sb.alarm.domain.model.Alarm
import com.sb.alarm.domain.usecase.AddAlarmUseCase
import com.sb.alarm.domain.usecase.GetAlarmsByDateUseCase
import com.sb.alarm.shared.RepeatType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val addAlarmUseCase: AddAlarmUseCase,
    private val getAlarmsByDateUseCase: GetAlarmsByDateUseCase,
) : ViewModel() {

    private val _selectedDateAlarms = MutableStateFlow<List<Alarm>>(emptyList())
    val selectedDateAlarms: StateFlow<List<Alarm>> = _selectedDateAlarms.asStateFlow()

    fun loadAlarmsForDate(date: LocalDate) {
        viewModelScope.launch {
            // 선택된 날짜에 해당하는 알람만 가져오기
            getAlarmsByDateUseCase.invoke(date).collect { alarmList ->
                _selectedDateAlarms.value = alarmList
            }
        }
    }

    /**
     * 매일 점심 복용 알람 추가
     */
    fun addDailyNoonAlarm() {
        viewModelScope.launch {
            addAlarmUseCase.invoke(
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
        }
    }
} 