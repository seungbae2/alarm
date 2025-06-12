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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.WeekCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.sb.alarm.domain.model.AlarmWithStatus
import com.sb.alarm.shared.RepeatType
import com.sb.alarm.shared.TakeStatus
import com.sb.alarm.util.toKoreanDateString
import com.sb.alarm.util.toKoreanMonth
import com.sb.alarm.util.toKoreanString
import com.sb.alarm.util.toKotlinDayOfWeek
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
    viewModel: ScheduleViewModel = hiltViewModel(),
) {
    val selectedDateAlarms by viewModel.selectedDateAlarms.collectAsState()
    val message by viewModel.message.collectAsState()
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedDate by remember { mutableStateOf(today) }

    LaunchedEffect(selectedDate) {
        viewModel.loadAlarmsForDate(selectedDate)
    }

    // 메시지 스낵바 표시
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // kotlinx.datetime으로 날짜 계산 후 java.time으로 변환
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
                onClick = viewModel::addDailyNoonAlarm
            ) {
                Icon(Icons.Default.Add, contentDescription = "매일 12시 알람 추가")
            }
        }
    ) { paddingValues ->
        ScheduleContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            selectedDate = selectedDate,
            selectedDateAlarms = selectedDateAlarms,
            calendarState = calendarState,
            onDateSelected = { date -> selectedDate = date }
        )
    }
}

@Composable
private fun ScheduleContent(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    selectedDateAlarms: List<AlarmWithStatus>,
    calendarState: WeekCalendarState,
    onDateSelected: (LocalDate) -> Unit,
) {
    Column(modifier = modifier) {
        CalendarSection(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            selectedDate = selectedDate,
            selectedDateAlarms = selectedDateAlarms,
            calendarState = calendarState,
            onDateSelected = onDateSelected
        )

        AlarmListSection(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            selectedDate = selectedDate,
            selectedDateAlarms = selectedDateAlarms,
        )
    }
}

@Composable
private fun CalendarSection(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    selectedDateAlarms: List<AlarmWithStatus>,
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
                        hasAlarm = selectedDateAlarms.isNotEmpty() && day.date.toKotlinLocalDate() == selectedDate,
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
) {
    Column(modifier = modifier) {
        AlarmListHeader(
            selectedDate = selectedDate,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        AlarmList(
            alarms = selectedDateAlarms,
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
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(alarms) { alarmWithStatus ->
            AlarmItem(alarmWithStatus = alarmWithStatus)
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
    hasAlarm: Boolean,
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

            if (hasAlarm) {
                AlarmIndicator(isSelected = isSelected)
            }
        }
    }
}

@Composable
private fun AlarmIndicator(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .size(4.dp)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
    )
}

@Composable
private fun AlarmItem(
    alarmWithStatus: AlarmWithStatus,
) {
    val alarm = alarmWithStatus.alarm
    val takeStatus = alarmWithStatus.takeStatus

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
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
                    AlarmTime(
                        hour = alarm.hour,
                        minute = alarm.minute
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
    takeStatus: TakeStatus?,
    actionTimestamp: Long?,
    modifier: Modifier = Modifier,
) {
    val (statusText, statusColor) = when (takeStatus) {
        TakeStatus.TAKEN -> "💊 복용 완료" to MaterialTheme.colorScheme.primary
        TakeStatus.SKIPPED -> "⏭️ 복용 스킵" to MaterialTheme.colorScheme.error
        null -> "⏰ 대기 중" to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(modifier = modifier) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodySmall,
            color = statusColor,
            fontWeight = if (takeStatus != null) FontWeight.Medium else FontWeight.Normal
        )

        // 처리 시간 표시 (처리된 경우만)
        if (takeStatus != null && actionTimestamp != null) {
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