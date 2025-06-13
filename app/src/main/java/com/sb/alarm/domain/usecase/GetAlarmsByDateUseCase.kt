package com.sb.alarm.domain.usecase

import com.sb.alarm.domain.model.Alarm
import com.sb.alarm.domain.model.AlarmWithStatus
import com.sb.alarm.domain.repository.AlarmRepository
import com.sb.alarm.shared.RepeatType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class GetAlarmsByDateUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository,
) {
    operator fun invoke(date: LocalDate): Flow<List<AlarmWithStatus>> {
        val dateString = date.toString() // "2024-01-15" 형식

        return combine(
            alarmRepository.getActiveAlarms(),
            alarmRepository.getHistoryByDate(dateString)
        ) { alarms, histories ->
            alarms.filter { alarm ->
                shouldAlarmTriggerOnDate(alarm, date)
            }.map { alarm ->
                val history = histories.find { it.alarmId == alarm.id }
                AlarmWithStatus(
                    alarm = alarm,
                    takeStatus = history?.status,
                    actionTimestamp = history?.actionTimestamp
                )
            }.sortedBy { it.alarm.hour * 60 + it.alarm.minute } // 시간순 오름차순 정렬
        }
    }

    private fun shouldAlarmTriggerOnDate(alarm: Alarm, targetDate: LocalDate): Boolean {
        val startDate = Instant.fromEpochMilliseconds(alarm.startDate)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

        // 시작 날짜보다 이전이면 알람 없음
        if (targetDate < startDate) {
            return false
        }

        // 종료 날짜가 설정되어 있고, 종료 날짜보다 이후이면 알람 없음
        alarm.endDate?.let { endTimestamp ->
            val endDate = Instant.fromEpochMilliseconds(endTimestamp)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
            if (targetDate > endDate) {
                return false
            }
        }

        return when (alarm.repeatType) {
            RepeatType.NONE -> {
                // 한 번만: 시작 날짜와 동일한 날짜에만 울림
                targetDate == startDate
            }

            RepeatType.DAILY -> {
                // 매일: 시작 날짜 이후 매일 울림
                true
            }

            RepeatType.WEEKLY -> {
                // 매주 특정 요일: repeatDaysOfWeek에 포함된 요일에만 울림
                alarm.repeatDaysOfWeek?.let { daysOfWeek ->
                    val targetDayOfWeek = getDayOfWeekNumber(targetDate.dayOfWeek)
                    daysOfWeek.contains(targetDayOfWeek)
                } == true
            }

            RepeatType.DAYS_INTERVAL -> {
                // N일 간격: 시작 날짜부터 repeatInterval 간격으로 울림
                val daysDiff = (targetDate.toEpochDays() - startDate.toEpochDays()).toInt()
                daysDiff >= 0 && daysDiff % alarm.repeatInterval == 0
            }
        }
    }

    private fun getDayOfWeekNumber(dayOfWeek: DayOfWeek): Int {
        return when (dayOfWeek) {
            DayOfWeek.SUNDAY -> 0
            DayOfWeek.MONDAY -> 1
            DayOfWeek.TUESDAY -> 2
            DayOfWeek.WEDNESDAY -> 3
            DayOfWeek.THURSDAY -> 4
            DayOfWeek.FRIDAY -> 5
            DayOfWeek.SATURDAY -> 6
        }
    }
} 