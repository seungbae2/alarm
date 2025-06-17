package com.sb.alarm.presentation.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.WeekCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.sb.alarm.domain.model.AlarmWithStatus
import com.sb.alarm.shared.constants.RepeatType
import com.sb.alarm.shared.constants.TakeStatus
import com.sb.alarm.shared.util.toKoreanDateString
import com.sb.alarm.shared.util.toKoreanMonth
import com.sb.alarm.shared.util.toKoreanString
import com.sb.alarm.shared.util.toKotlinDayOfWeek
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.todayIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    navController: NavController,
    viewModel: ScheduleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    var selectedDate by remember { mutableStateOf(today) }
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedAlarm by remember { mutableStateOf<AlarmWithStatus?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    val bottomSheetState = rememberModalBottomSheetState()
    rememberCoroutineScope()

    LaunchedEffect(selectedDate) {
        viewModel.onEvent(ScheduleEvent.LoadAlarms(selectedDate))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect {
            when (it) {
                is ScheduleEffect.ShowToast -> snackbarHostState.showSnackbar(it.message)
                is ScheduleEffect.NavigateToEditSchedule -> {
                    navController.navigate("updateSchedule/${it.alarmWithStatus.alarm.id}")
                }
            }
        }
    }

    val startDate = remember { today.minus(DatePeriod(months = 6)).toJavaLocalDate() }
    val endDate = remember { today.plus(DatePeriod(months = 6)).toJavaLocalDate() }

    val calendarState = rememberWeekCalendarState(
        startDate = startDate,
        endDate = endDate,
        firstVisibleWeekDate = today.toJavaLocalDate(),
        firstDayOfWeek = firstDayOfWeekFromLocale()
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("알람 스케줄") }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(ScheduleEvent.AddAlarm) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "매일 12시 알람 추가")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            CalendarSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                selectedDate = selectedDate,
                calendarState = calendarState,
                onDateSelected = { date -> selectedDate = date }
            )

            when (uiState) {
                is ScheduleUiState.Loading -> {}
                is ScheduleUiState.Success -> {
                    AlarmListSection(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        selectedDate = selectedDate,
                        selectedDateAlarms = (uiState as ScheduleUiState.Success).alarms,
                        onAlarmClick = { alarm ->
                            selectedAlarm = alarm
                            showBottomSheet = true
                        }
                    )
                }

                is ScheduleUiState.Error -> {}
            }
        }
    }

    // ModalBottomSheet
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = bottomSheetState
        ) {
            AlarmActionBottomSheet(
                selectedAlarm = selectedAlarm,
                onSetAlarmInOneMinute = {
                    selectedAlarm?.let { alarm ->
                        viewModel.onEvent(ScheduleEvent.SetAlarmInOneMinute(alarm))
                    }
                    showBottomSheet = false
                },
                onEditSchedule = {
                    selectedAlarm?.let { alarm ->
                        viewModel.onEvent(ScheduleEvent.EditSchedule(alarm))
                    }
                    showBottomSheet = false
                },
                onDismiss = {
                    showBottomSheet = false
                }
            )
        }
    }
}

@Composable
private fun CalendarSection(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    calendarState: WeekCalendarState,
    onDateSelected: (LocalDate) -> Unit,
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            CalendarHeader(
                calendarState = calendarState,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            WeekCalendar(
                state = calendarState,
                weekHeader = {
                    val daysOfWeek = daysOfWeek(firstDayOfWeek = java.time.DayOfWeek.SUNDAY)
                    WeekHeader(daysOfWeek = daysOfWeek.map { it.toKotlinDayOfWeek() })
                },
                dayContent = { day ->
                    DayContent(
                        day = day,
                        isSelected = day.date.toKotlinLocalDate() == selectedDate,
                        onClick = { onDateSelected(day.date.toKotlinLocalDate()) }
                    )
                }
            )
        }
    }
}

@Composable
private fun CalendarHeader(
    calendarState: WeekCalendarState,
    modifier: Modifier = Modifier,
) {
    val visibleWeek = calendarState.firstVisibleWeek
    val firstDayOfWeek = visibleWeek.days.first().date.toKotlinLocalDate()
    val monthName = firstDayOfWeek.monthNumber.toKoreanMonth()
    val year = firstDayOfWeek.year

    Text(
        text = "$monthName $year",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
private fun AlarmListSection(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    selectedDateAlarms: List<AlarmWithStatus>,
    onAlarmClick: (AlarmWithStatus) -> Unit = {},
) {
    Column(modifier = modifier) {
        AlarmListHeader(
            selectedDate = selectedDate,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        AlarmList(
            alarms = selectedDateAlarms,
            onAlarmClick = onAlarmClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
    }
}

@Composable
private fun AlarmListHeader(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "${selectedDate.toKoreanDateString()} 알람",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
private fun AlarmList(
    alarms: List<AlarmWithStatus>,
    modifier: Modifier = Modifier,
    onAlarmClick: (AlarmWithStatus) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(alarms) { alarmWithStatus ->
            AlarmItem(
                alarmWithStatus = alarmWithStatus,
                onClick = { onAlarmClick(alarmWithStatus) }
            )
            Spacer(modifier = Modifier.height(height = 2.dp))
        }

        if (alarms.isEmpty()) {
            item {
                EmptyAlarmCard()
            }
        }
    }
}

@Composable
private fun EmptyAlarmCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = "이 날짜에는 알람이 없습니다.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        )
    }
}

@Composable
private fun WeekHeader(daysOfWeek: List<DayOfWeek>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        daysOfWeek.forEach { dayOfWeek ->
            Text(
                text = dayOfWeek.toKoreanString(),
                modifier = Modifier
                    .weight(1f)
                    .size(48.dp)
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DayContent(
    day: WeekDay,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val dayAsKotlinDate = day.date.toKotlinLocalDate()
    val isToday = dayAsKotlinDate == today

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayAsKotlinDate.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
            )

        }
    }
}

@Composable
private fun AlarmItem(
    alarmWithStatus: AlarmWithStatus,
    onClick: () -> Unit = {},
) {
    val alarm = alarmWithStatus.alarm
    val takeStatus = alarmWithStatus.takeStatus

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        // 1분뒤 알람 구분을 위한 색상 변경
        colors = if (alarmWithStatus.oneMinuteLaterTime != null) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // 1분뒤 알람이 설정된 경우 표시
                    if (alarmWithStatus.oneMinuteLaterTime != null) {
                        Text(
                            text = "⏰ 1분 후 알람 (${alarmWithStatus.oneMinuteLaterTime})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    // 1분뒤 알람이 설정된 경우 1분뒤 시간을 표시, 아니면 원래 시간 표시
                    val displayTime = alarmWithStatus.oneMinuteLaterTime ?: "${
                        alarm.hour.toString().padStart(2, '0')
                    }:${alarm.minute.toString().padStart(2, '0')}"

                    AlarmTimeDisplay(
                        timeText = displayTime,
                        isOneMinuteLater = alarmWithStatus.oneMinuteLaterTime != null
                    )

                    AlarmMedicationName(
                        medicationName = alarm.medicationName,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    AlarmRepeatInfo(
                        repeatType = alarm.repeatType,
                        repeatInterval = alarm.repeatInterval,
                        repeatDaysOfWeek = alarm.repeatDaysOfWeek,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    AlarmTakeStatus(
                        takeStatus = takeStatus,
                        actionTimestamp = alarmWithStatus.actionTimestamp,
                        isOneMinuteLaterAlarm = alarmWithStatus.isOneMinuteLaterAlarm,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmTime(
    hour: Int,
    minute: Int,
) {
    Text(
        text = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun AlarmTimeDisplay(
    timeText: String,
    isOneMinuteLater: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = timeText,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = if (isOneMinuteLater) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

@Composable
private fun AlarmMedicationName(
    medicationName: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = medicationName,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

@Composable
private fun AlarmRepeatInfo(
    repeatType: RepeatType,
    repeatInterval: Int,
    repeatDaysOfWeek: List<Int>?,
    modifier: Modifier = Modifier,
) {
    val repeatText = when (repeatType) {
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

    Text(
        text = repeatText,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
private fun AlarmTakeStatus(
    takeStatus: TakeStatus,
    actionTimestamp: Long?,
    isOneMinuteLaterAlarm: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val (statusText, statusColor) = when (takeStatus) {
        TakeStatus.TAKEN -> "💊 복용 완료" to MaterialTheme.colorScheme.primary
        TakeStatus.SKIPPED -> "⏭️ 복용 스킵" to MaterialTheme.colorScheme.error
        TakeStatus.NOT_ACTION -> "⏰ 대기 중" to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(modifier = modifier) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodySmall,
            color = statusColor,
            fontWeight = if (takeStatus != TakeStatus.NOT_ACTION) FontWeight.Medium else FontWeight.Normal
        )

        // 1분뒤 알람으로 인한 히스토리인 경우 표시
        if (isOneMinuteLaterAlarm && takeStatus != TakeStatus.NOT_ACTION) {
            Text(
                text = "🔔 1분뒤 알람으로 처리됨",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // 처리 시간 표시 (처리된 경우만)
        if (takeStatus != TakeStatus.NOT_ACTION && actionTimestamp != null) {
            val timeString = remember(actionTimestamp) {
                SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(Date(actionTimestamp))
            }
            Text(
                text = "처리 시간: $timeString",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun AlarmActionBottomSheet(
    selectedAlarm: AlarmWithStatus?,
    onSetAlarmInOneMinute: () -> Unit,
    onEditSchedule: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        selectedAlarm?.let { alarm ->
            Text(
                text = "${alarm.alarm.medicationName} 알람",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "${
                    alarm.alarm.hour.toString().padStart(2, '0')
                }:${alarm.alarm.minute.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // 이미 완료된 알람인지 확인
        val shouldShowOneMinuteButton = selectedAlarm?.takeStatus == TakeStatus.NOT_ACTION

        // 1분뒤 알람 버튼 (조건에 맞는 경우만 표시)
        if (shouldShowOneMinuteButton) {
            Button(
                onClick = onSetAlarmInOneMinute,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("1분뒤에 알람 울리기")
            }
        }

        // 스케줄 수정 버튼 (항상 표시)
        Button(
            onClick = onEditSchedule,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("스케줄 수정하기")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
} 