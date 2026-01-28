package com.sb.alarm.presentation.alarm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sb.alarm.domain.usecase.GetAlarmByIdUseCase
import com.sb.alarm.domain.usecase.HandleAlarmCompletionUseCase
import com.sb.alarm.shared.constants.TakeStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val getAlarmByIdUseCase: GetAlarmByIdUseCase,
    private val handleAlarmCompletionUseCase: HandleAlarmCompletionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlarmUiState>(AlarmUiState.Loading)
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    private val _alarmEffect = Channel<AlarmEffect>(Channel.BUFFERED)
    val alarmEffect = _alarmEffect.receiveAsFlow()

    fun loadAlarm(alarmId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = AlarmUiState.Loading

                val alarmInfo = getAlarmByIdUseCase(alarmId)

                if (alarmInfo != null) {
                    val currentTime = getCurrentTimeString()
                    val currentDate = getCurrentDateString()

                    _uiState.value = AlarmUiState.Success(
                        alarm = alarmInfo,
                        currentTime = currentTime,
                        currentDate = currentDate
                    )
                } else {
                    Log.e("AlarmViewModel", "Alarm not found for ID: $alarmId")
                    _uiState.value = AlarmUiState.Error
                }
            } catch (e: Exception) {
                Log.e("AlarmViewModel", "Failed to load alarm", e)
                _uiState.value = AlarmUiState.Error
            }
        }
    }

    fun onEvent(event: AlarmUiEvent) {
        when (event) {
            is AlarmUiEvent.TakeCompleted -> handleTakeCompleted(event.alarmId, event.isOneMinuteLaterAlarm)
            is AlarmUiEvent.Dismiss -> handleDismiss(event.alarmId, event.isOneMinuteLaterAlarm)
        }
    }

    private fun handleTakeCompleted(alarmId: Int, isOneMinuteLaterAlarm: Boolean = false) {
        viewModelScope.launch {
            try {
                val currentUiState = _uiState.value
                if (currentUiState is AlarmUiState.Success) {
                    handleAlarmCompletionUseCase(
                        alarm = currentUiState.alarm,
                        alarmId = alarmId,
                        status = TakeStatus.TAKEN,
                        isOneMinuteLaterAlarm = isOneMinuteLaterAlarm
                    )
                    _alarmEffect.send(AlarmEffect.NavigateToSchedule)
                }
            } catch (e: Exception) {
                Log.e("AlarmViewModel", "Failed to mark as taken", e)
            }
        }
    }

    private fun handleDismiss(alarmId: Int, isOneMinuteLaterAlarm: Boolean = false) {
        viewModelScope.launch {
            try {
                val currentUiState = _uiState.value
                if (currentUiState is AlarmUiState.Success) {
                    handleAlarmCompletionUseCase(
                        alarm = currentUiState.alarm,
                        alarmId = alarmId,
                        status = TakeStatus.SKIPPED,
                        isOneMinuteLaterAlarm = isOneMinuteLaterAlarm
                    )
                    _alarmEffect.send(AlarmEffect.NavigateToScheduleAfterDismiss)
                }
            } catch (e: Exception) {
                Log.e("AlarmViewModel", "Failed to dismiss alarm", e)
            }
        }
    }

    fun requestStopAlarm() {
        viewModelScope.launch {
            _alarmEffect.send(AlarmEffect.StopAlarmService)
        }
    }

    private fun getCurrentTimeString(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    private fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault()).format(Date())
    }
} 