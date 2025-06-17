package com.sb.alarm.presentation.updateSchedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sb.alarm.shared.constants.RepeatType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateScheduleScreen(
    alarmId: Int,
    navController: NavController,
    viewModel: UpdateScheduleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(alarmId) {
        viewModel.onEvent(UpdateScheduleEvent.LoadAlarm(alarmId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UpdateScheduleEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is UpdateScheduleEffect.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("알람 정보") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(UpdateScheduleEvent.NavigateBack) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로 가기")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isLoading) {
                Text("알람 정보를 불러오는 중...")
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                uiState.alarm?.let { alarm ->
                    // 약물명 표시
                    AlarmInfoCard(
                        title = "약물명",
                        content = alarm.medicationName
                    )

                    // 시간 표시
                    AlarmInfoCard(
                        title = "알람 시간",
                        content = "${alarm.hour.toString().padStart(2, '0')}:${alarm.minute.toString().padStart(2, '0')}"
                    )

                    // 반복 설정 표시
                    AlarmInfoCard(
                        title = "반복 설정",
                        content = getRepeatTypeText(alarm.repeatType, alarm.repeatInterval, alarm.repeatDaysOfWeek)
                    )

                    // 활성화 상태 표시
                    AlarmInfoCard(
                        title = "상태",
                        content = if (alarm.isActive) "활성화" else "비활성화"
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmInfoCard(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun getRepeatTypeText(
    repeatType: RepeatType,
    repeatInterval: Int,
    repeatDaysOfWeek: List<Int>?
): String {
    return when (repeatType) {
        RepeatType.DAILY -> "매일"
        RepeatType.WEEKLY -> {
            val days = repeatDaysOfWeek?.joinToString(", ") { dayNumber ->
                when (dayNumber) {
                    0 -> "일"
                    1 -> "월"
                    2 -> "화"
                    3 -> "수"
                    4 -> "목"
                    5 -> "금"
                    6 -> "토"
                    else -> "$dayNumber"
                }
            } ?: "매주"
            "매주 ($days)"
        }
        RepeatType.DAYS_INTERVAL -> "${repeatInterval}일 간격"
        RepeatType.NONE -> "한 번만"
    }
}