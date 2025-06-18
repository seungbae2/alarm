package com.sb.alarm.presentation.updateSchedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

data class AlarmTime(val hour: Int, val minute: Int)
data class AlternatingStep(
    val times: List<AlarmTime>,
    val durationDays: Int,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateScheduleScreen(
    alarmId: Int,
    navController: NavController,
    viewModel: UpdateScheduleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // UI 상태
    var selectedType by remember { mutableIntStateOf(1) } // 1: Daily, 2: Alternating
    var dailyTime by remember { mutableStateOf(AlarmTime(7, 0)) }
    var startDate by remember { mutableStateOf("2025.07.11") }
    var alternatingSteps by remember {
        mutableStateOf(
            listOf(
                AlternatingStep(
                    times = listOf(
                        AlarmTime(7, 0),
                        AlarmTime(12, 0),
                        AlarmTime(18, 0)
                    ),
                    durationDays = 2
                ),
                AlternatingStep(
                    times = listOf(AlarmTime(10, 0)),
                    durationDays = 1
                )
            )
        )
    }

    LaunchedEffect(alarmId) {
        viewModel.onEvent(UpdateScheduleEvent.LoadAlarm(alarmId))
    }

    // 알람 데이터가 로드되면 dailyTime 초기화
    LaunchedEffect(Unit) {
        viewModel.uiState.collect { state ->
            if (state is UpdateScheduleUiState.Success) {
                dailyTime = AlarmTime(state.alarm.hour, state.alarm.minute)
            }
        }
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

                is UpdateScheduleEffect.UpdateSuccess -> {
                    navController.popBackStack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("알람 주기 변경") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(UpdateScheduleEvent.NavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로 가기")
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 상단 영역: 알람주기 변경
            AlarmCycleSelectionCard(
                selectedType = selectedType,
                onTypeSelected = { selectedType = it }
            )

            // 하단 영역: 선택된 주기 알람시간 변경
            AlarmTimeSettingCard(
                selectedType = selectedType,
                dailyTime = dailyTime,
                onDailyTimeChange = { dailyTime = it },
                startDate = startDate,
                onStartDateChange = { startDate = it },
                alternatingSteps = alternatingSteps,
                onAlternatingStepsChange = { alternatingSteps = it }
            )

            // 저장 버튼
            Button(
                onClick = {
                    if (selectedType == 1) {
                        // 매일 한번씩 같은시간 선택 시 dailyTime 사용
                        viewModel.onEvent(
                            UpdateScheduleEvent.UpdateAlarm(
                                hour = dailyTime.hour,
                                minute = dailyTime.minute
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("변경사항 저장")
            }
        }
    }
}

@Composable
private fun AlarmCycleSelectionCard(
    selectedType: Int,
    onTypeSelected: (Int) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "알람주기 변경",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 타입 1: 매일 한번씩 같은시간
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedType == 1,
                        onClick = { onTypeSelected(1) }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedType == 1,
                    onClick = { onTypeSelected(1) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "매일 한번씩 같은시간",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "하루에 한 번 동일한 시간에 알람",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 타입 2: 매일 횟수 교대형 알람 주기
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedType == 2,
                        onClick = { onTypeSelected(2) }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedType == 2,
                    onClick = { onTypeSelected(2) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "매일 횟수 교대형 알람 주기",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "복용 패턴에 따라 매일 다른 횟수로 알람",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmTimeSettingCard(
    selectedType: Int,
    dailyTime: AlarmTime,
    onDailyTimeChange: (AlarmTime) -> Unit,
    startDate: String,
    onStartDateChange: (String) -> Unit,
    alternatingSteps: List<AlternatingStep>,
    onAlternatingStepsChange: (List<AlternatingStep>) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "선택된 주기 알람시간 변경",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (selectedType == 1) {
                // 타입 1: 매일 한번씩 같은시간
                DailyTimeSelection(
                    time = dailyTime,
                    onTimeChange = onDailyTimeChange
                )
            } else {
                // 타입 2: 매일 횟수 교대형 알람 주기
                AlternatingCycleSelection(
                    startDate = startDate,
                    onStartDateChange = onStartDateChange,
                    steps = alternatingSteps,
                    onStepsChange = onAlternatingStepsChange
                )
            }
        }
    }
}

@Composable
private fun DailyTimeSelection(
    time: AlarmTime,
    onTimeChange: (AlarmTime) -> Unit,
) {
    Column {
        Text(
            text = "매일 한번씩 같은시간에",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeSelector(
                hour = time.hour,
                minute = time.minute,
                onTimeChange = { hour, minute ->
                    onTimeChange(AlarmTime(hour, minute))
                }
            )
        }
    }
}

@Composable
private fun AlternatingCycleSelection(
    startDate: String,
    onStartDateChange: (String) -> Unit,
    steps: List<AlternatingStep>,
    onStepsChange: (List<AlternatingStep>) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    Column {
        Text(
            text = "매일 횟수 교대형 알람 주기",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 시작 날짜
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("시작일: ")
            OutlinedButton(
                onClick = { showDatePicker = true }
            ) {
                Text(startDate)
            }
            Text(" 부터 아래 패턴을 반복")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 단계별 설정 (최대 2개 패턴)
        steps.forEachIndexed { index, step ->
            StepCard(
                stepNumber = index + 1,
                step = step,
                onStepChange = { newStep ->
                    val newSteps = steps.toMutableList()
                    newSteps[index] = newStep
                    onStepsChange(newSteps)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 날짜 선택 다이얼로그
        if (showDatePicker) {
            DatePickerDialog(
                onDateSelected = { selectedDate ->
                    onStartDateChange(selectedDate)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

@Composable
private fun StepCard(
    stepNumber: Int,
    step: AlternatingStep,
    onStepChange: (AlternatingStep) -> Unit,
) {
    var showDurationDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "패턴 $stepNumber:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("하루 ")
                Text(
                    text = "${step.times.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("회씩 ")
                OutlinedButton(
                    onClick = {
                        showDurationDialog = true
                    }
                ) {
                    Text(
                        text = "${step.durationDays}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text("일 동안 ")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 시간 목록
            step.times.forEachIndexed { timeIndex, alarmTime ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    TimeSelector(
                        hour = alarmTime.hour,
                        minute = alarmTime.minute,
                        onTimeChange = { hour, minute ->
                            val newTimes = step.times.toMutableList()
                            newTimes[timeIndex] = AlarmTime(hour, minute)
                            onStepChange(step.copy(times = newTimes))
                        }
                    )

                    if (step.times.size > 1) {
                        IconButton(
                            onClick = {
                                val newTimes = step.times.toMutableList()
                                newTimes.removeAt(timeIndex)
                                onStepChange(step.copy(times = newTimes))
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "시간 제거")
                        }
                    }
                }
            }

            // 시간 추가 버튼
            OutlinedButton(
                onClick = {
                    val newTimes = step.times + AlarmTime(9, 0)
                    onStepChange(step.copy(times = newTimes))
                }
            ) {
                Text("알람 시간 추가")
            }
        }
    }

    // 일수 선택 다이얼로그
    if (showDurationDialog) {
        DurationPickerDialog(
            currentDuration = step.durationDays,
            onDurationSelected = { newDuration ->
                onStepChange(step.copy(durationDays = newDuration))
                showDurationDialog = false
            },
            onDismiss = { showDurationDialog = false }
        )
    }
}

@Composable
private fun TimeSelector(
    hour: Int,
    minute: Int,
    onTimeChange: (Int, Int) -> Unit,
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = { showTimePicker = true }
        ) {
            Text(
                text = "${if (hour == 0) 12 else if (hour > 12) hour - 12 else hour}:${
                    minute.toString().padStart(2, '0')
                } ${if (hour < 12) "오전" else "오후"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            currentHour = hour,
            currentMinute = minute,
            onTimeSelected = { selectedHour, selectedMinute ->
                onTimeChange(selectedHour, selectedMinute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    currentHour: Int,
    currentMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val timePickerState = rememberTimePickerState(
        initialHour = currentHour,
        initialMinute = currentMinute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "시간 선택",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            TimePicker(
                state = timePickerState,
                layoutType = TimePickerLayoutType.Vertical
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(timePickerState.hour, timePickerState.minute)
                }
            ) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
private fun DurationPickerDialog(
    currentDuration: Int,
    onDurationSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedDuration by remember { mutableStateOf(currentDuration) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "일수 선택",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "몇 일 동안 반복할지 선택하세요",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 감소 버튼
                    OutlinedButton(
                        onClick = {
                            if (selectedDuration > 1) {
                                selectedDuration--
                            }
                        },
                        modifier = Modifier.width(50.dp)
                    ) {
                        Text("-")
                    }

                    // 현재 일수 표시
                    Text(
                        text = "${selectedDuration}일",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(60.dp)
                    )

                    // 증가 버튼
                    OutlinedButton(
                        onClick = {
                            if (selectedDuration < 30) { // 최대 30일로 제한
                                selectedDuration++
                            }
                        },
                        modifier = Modifier.width(50.dp)
                    ) {
                        Text("+")
                    }
                }

                Text(
                    text = "1일 ~ 30일까지 설정 가능합니다",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDurationSelected(selectedDuration)
                }
            ) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis)
                        val localDate =
                            java.time.LocalDate.ofInstant(date, java.time.ZoneId.systemDefault())
                        val formattedDate = "${localDate.year}.${
                            localDate.monthValue.toString().padStart(2, '0')
                        }.${localDate.dayOfMonth.toString().padStart(2, '0')}"
                        onDateSelected(formattedDate)
                    }
                }
            ) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = "시작 날짜 선택",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }
        )
    }
}