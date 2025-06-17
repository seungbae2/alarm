package com.sb.alarm.presentation.alarm

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sb.alarm.domain.model.Alarm
import com.sb.alarm.domain.model.AlarmHistory
import com.sb.alarm.domain.repository.AlarmRepository
import com.sb.alarm.domain.repository.AlarmSchedulerRepository
import com.sb.alarm.presentation.service.AlarmService
import com.sb.alarm.shared.constants.RepeatType
import com.sb.alarm.shared.constants.TakeStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val alarmSchedulerRepository: AlarmSchedulerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlarmUiState>(AlarmUiState.Loading)
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    private val _alarmEffect = Channel<AlarmEffect>(Channel.BUFFERED)
    val alarmEffect = _alarmEffect.receiveAsFlow()

    fun loadAlarm(alarmId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = AlarmUiState.Loading

                val alarmInfo = alarmRepository.getAlarmById(alarmId)

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

    fun onEvent(event: AlarmUiEvent, alarmId: Int, isOneMinuteLaterAlarm: Boolean = false) {
        when (event) {
            is AlarmUiEvent.TakeCompleted -> handleTakeCompleted(alarmId, isOneMinuteLaterAlarm)
            is AlarmUiEvent.Dismiss -> handleDismiss(alarmId, isOneMinuteLaterAlarm)
        }
    }

    private fun handleTakeCompleted(alarmId: Int, isOneMinuteLaterAlarm: Boolean = false) {
        viewModelScope.launch {
            try {
                val currentUiState = _uiState.value
                if (currentUiState is AlarmUiState.Success) {
                    saveAlarmHistory(alarmId, TakeStatus.TAKEN, isOneMinuteLaterAlarm)
                    scheduleNextAlarmIfRepeating(currentUiState.alarm, alarmId)
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
                    saveAlarmHistory(alarmId, TakeStatus.SKIPPED, isOneMinuteLaterAlarm)
                    scheduleNextAlarmIfRepeating(currentUiState.alarm, alarmId)
                    _alarmEffect.send(AlarmEffect.NavigateToScheduleAfterDismiss)
                }
            } catch (e: Exception) {
                Log.e("AlarmViewModel", "Failed to dismiss alarm", e)
            }
        }
    }

    fun stopAlarmFromActivity(context: Context) {
        try {
            val intent = Intent(context, AlarmService::class.java).apply {
                action = AlarmService.ACTION_STOP_ALARM
            }
            context.startService(intent)
            Log.d("AlarmViewModel", "Stop alarm service command sent")
        } catch (e: Exception) {
            Log.e("AlarmViewModel", "Failed to send stop alarm service command", e)
        }
    }

    // Private helper methods

    private suspend fun saveAlarmHistory(alarmId: Int, status: TakeStatus, isOneMinuteLaterAlarm: Boolean = false) {
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date.toString()

        val history = AlarmHistory(
            alarmId = alarmId,
            logDate = today,
            status = status,
            actionTimestamp = System.currentTimeMillis(),
            isOneMinuteLaterAlarm = isOneMinuteLaterAlarm
        )
        alarmRepository.saveAlarmHistory(history)
    }

    private suspend fun scheduleNextAlarmIfRepeating(alarm: Alarm, alarmId: Int) {
        when (alarm.repeatType) {
            RepeatType.NONE -> {
                Log.d("AlarmViewModel", "One-time alarm - deactivating")
                alarmRepository.updateAlarmActiveStatus(alarmId, false)
            }

            RepeatType.DAILY -> {
                Log.d("AlarmViewModel", "Daily alarm - rescheduling for next day")
                alarmSchedulerRepository.schedule(alarm)
            }

            else -> {
                Log.d("AlarmViewModel", "Repeating alarm - rescheduling")
                alarmSchedulerRepository.schedule(alarm)
            }
        }
    }

    private fun getCurrentTimeString(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    private fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault()).format(Date())
    }
} 