package com.sb.alarm.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.sb.alarm.domain.model.Alarm
import com.sb.alarm.domain.repository.AlarmSchedulerRepository
import com.sb.alarm.presentation.receiver.AlarmReceiver
import com.sb.alarm.shared.RepeatType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class AlarmSchedulerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AlarmSchedulerRepository {

    private val alarmManager by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService(AlarmManager::class.java)
        } else {
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        }
    }

    override fun schedule(alarmItem: Alarm) {
        try {
            // 비활성 알람은 스케줄링하지 않음
            if (!alarmItem.isActive) {
                Log.w("AlarmScheduler", "Skipping inactive alarm: ${alarmItem.id}")
                return
            }

            // Android 12+ 권한 확인
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.e(
                        "AlarmScheduler",
                        "SCHEDULE_EXACT_ALARM permission not granted for alarm: ${alarmItem.id}"
                    )
                    // 정확한 알람을 설정할 수 없지만 기본 알람으로 시도
                }
            }

            // alarmItem을 기반으로 다음 알람 시간을 계산
            val triggerAtMillis = calculateNextTriggerTime(alarmItem)

            if (triggerAtMillis <= 0) {
                Log.e(
                    "AlarmScheduler",
                    "Invalid trigger time calculated for alarm: ${alarmItem.id}"
                )
                return
            }

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("ALARM_ID", alarmItem.id)
                putExtra("MEDICATION_NAME", alarmItem.medicationName)
                putExtra("REPEAT_TYPE", alarmItem.repeatType.name)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmItem.id, // 각 알람마다 고유한 requestCode 사용
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )

            // Android 버전과 권한에 따른 적절한 알람 설정
            scheduleAlarmBasedOnVersion(triggerAtMillis, pendingIntent, alarmItem)

        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Failed to schedule alarm: ${alarmItem.id}", e)
            throw e // 상위 레이어에서 처리할 수 있도록 다시 던짐
        }
    }

    private fun scheduleAlarmBasedOnVersion(
        triggerAtMillis: Long,
        pendingIntent: PendingIntent,
        alarm: Alarm,
    ) {
        try {
            val triggerTimeStr = java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                java.util.Locale.getDefault()
            ).format(java.util.Date(triggerAtMillis))

            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                    Log.d(
                        "AlarmScheduler",
                        "Scheduled alarm ID ${alarm.id} (${alarm.medicationName}) with setExactAndAllowWhileIdle for $triggerTimeStr"
                    )
                }
                // Android 6.0 (API 23) 이상에서는 setAndAllowWhileIdle 사용
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                    Log.d(
                        "AlarmScheduler",
                        "Scheduled alarm ID ${alarm.id} (${alarm.medicationName}) with setAndAllowWhileIdle for $triggerTimeStr"
                    )
                }

                // 이전 버전에서는 기본 set 사용
                else -> {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                    Log.d(
                        "AlarmScheduler",
                        "Scheduled alarm ID ${alarm.id} (${alarm.medicationName}) with basic set for $triggerTimeStr"
                    )
                }
            }
        } catch (e: SecurityException) {
            // 권한 문제가 발생한 경우 fallback으로 기본 알람 사용
            Log.w("AlarmScheduler", "SecurityException occurred, falling back to basic alarm", e)
            try {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.d("AlarmScheduler", "Fallback: Scheduled basic alarm ID ${alarm.id}")
            } catch (e2: Exception) {
                Log.e(
                    "AlarmScheduler",
                    "Failed to schedule alarm ID ${alarm.id} even with fallback",
                    e2
                )
                throw e2 // 상위 레이어에서 처리할 수 있도록 다시 던짐
            }
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Unexpected error while scheduling alarm ID ${alarm.id}", e)
            throw e // 상위 레이어에서 처리할 수 있도록 다시 던짐
        }
    }

    override fun cancel(alarmId: Int) {
        try {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_NO_CREATE
                }
            )

            // PendingIntent가 존재하면 알람 취소
            pendingIntent?.let {
                try {
                    alarmManager.cancel(it)
                    it.cancel()
                    Log.d("AlarmScheduler", "Successfully cancelled alarm with ID: $alarmId")
                } catch (e: Exception) {
                    Log.e("AlarmScheduler", "Failed to cancel alarm with ID: $alarmId", e)
                    throw e
                }
            } ?: run {
                Log.w("AlarmScheduler", "No existing alarm found for ID: $alarmId")
            }
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Error during alarm cancellation for ID: $alarmId", e)
            throw e
        }
    }

    /**
     * 알람 설정에 따라 다음 알람 발생 시간을 계산합니다.
     */
    private fun calculateNextTriggerTime(alarm: Alarm): Long {
        try {
            val currentTime = Clock.System.now()
            val timeZone = TimeZone.currentSystemDefault()
            val today = currentTime.toLocalDateTime(timeZone).date

            Log.d(
                "AlarmScheduler",
                "Calculating trigger time for alarm ${alarm.id}: current=$currentTime, today=$today, hour=${alarm.hour}, minute=${alarm.minute}, repeatType=${alarm.repeatType}"
            )

            return when (alarm.repeatType) {
                RepeatType.NONE -> {
                    // 한 번만: startDate의 해당 시간, 하지만 과거 시간이면 다음날로 설정
                    val startDate = Instant.fromEpochMilliseconds(alarm.startDate)
                        .toLocalDateTime(timeZone).date
                    val alarmDateTime = startDate.atTime(alarm.hour, alarm.minute)
                    val alarmInstant = alarmDateTime.toInstant(timeZone)

                    Log.d(
                        "AlarmScheduler",
                        "RepeatType.NONE: startDate=$startDate, alarmDateTime=$alarmDateTime, alarmInstant=$alarmInstant"
                    )

                    // 알람 시간이 현재 시간보다 과거이면 다음날로 설정
                    if (alarmInstant <= currentTime) {
                        val tomorrowAlarmTime = today.plus(1, DateTimeUnit.DAY)
                            .atTime(alarm.hour, alarm.minute)
                        val result = tomorrowAlarmTime.toInstant(timeZone).toEpochMilliseconds()
                        Log.d(
                            "AlarmScheduler",
                            "Alarm time is in past, rescheduling to tomorrow: $tomorrowAlarmTime -> $result"
                        )
                        result
                    } else {
                        val result = alarmInstant.toEpochMilliseconds()
                        Log.d(
                            "AlarmScheduler",
                            "Alarm time is in future, scheduling as-is: $alarmInstant -> $result"
                        )
                        result
                    }
                }

                RepeatType.DAILY -> {
                    // 매일: 오늘 해당 시간이 이미 지났으면 내일, 아니면 오늘
                    val todayAlarmTime = today.atTime(alarm.hour, alarm.minute)
                    val todayAlarmInstant = todayAlarmTime.toInstant(timeZone)

                    if (currentTime >= todayAlarmInstant) {
                        // 오늘 알람 시간이 이미 지났으면 내일
                        val tomorrowAlarmTime = today.plus(1, DateTimeUnit.DAY)
                            .atTime(alarm.hour, alarm.minute)
                        tomorrowAlarmTime.toInstant(timeZone).toEpochMilliseconds()
                    } else {
                        // 오늘 알람 시간이 아직 안 지났으면 오늘
                        todayAlarmInstant.toEpochMilliseconds()
                    }
                }

                RepeatType.WEEKLY -> {
                    // 매주 특정 요일: repeatDaysOfWeek에 포함된 가장 가까운 미래 요일
                    val daysOfWeek = alarm.repeatDaysOfWeek ?: return 0L
                    calculateNextWeeklyAlarm(
                        today,
                        alarm.hour,
                        alarm.minute,
                        daysOfWeek,
                        timeZone,
                        currentTime
                    )
                }

                RepeatType.DAYS_INTERVAL -> {
                    // N일 간격: startDate부터 repeatInterval 간격으로 계산
                    val startDate = Instant.fromEpochMilliseconds(alarm.startDate)
                        .toLocalDateTime(timeZone).date
                    calculateNextIntervalAlarm(
                        startDate,
                        today,
                        alarm.hour,
                        alarm.minute,
                        alarm.repeatInterval,
                        timeZone,
                        currentTime
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Error calculating next trigger time for alarm ${alarm.id}", e)
            return 0L
        }
    }

    private fun calculateNextWeeklyAlarm(
        today: LocalDate,
        hour: Int,
        minute: Int,
        daysOfWeek: List<Int>,
        timeZone: TimeZone,
        currentTime: Instant,
    ): Long {
        val todayNumber = getDayOfWeekNumber(today.dayOfWeek)

        // 오늘이 알람 요일 중 하나이고, 아직 시간이 안 지났다면 오늘
        if (daysOfWeek.contains(todayNumber)) {
            val todayAlarmTime = today.atTime(hour, minute)
            val todayAlarmInstant = todayAlarmTime.toInstant(timeZone)
            if (currentTime < todayAlarmInstant) {
                return todayAlarmInstant.toEpochMilliseconds()
            }
        }

        // 다음 알람 요일 찾기
        for (i in 1..7) {
            val nextDate = today.plus(i, DateTimeUnit.DAY)
            val nextDayNumber = getDayOfWeekNumber(nextDate.dayOfWeek)
            if (daysOfWeek.contains(nextDayNumber)) {
                val nextAlarmTime = nextDate.atTime(hour, minute)
                return nextAlarmTime.toInstant(timeZone).toEpochMilliseconds()
            }
        }

        return 0L // 이 경우는 발생하지 않아야 함
    }

    private fun calculateNextIntervalAlarm(
        startDate: LocalDate,
        today: LocalDate,
        hour: Int,
        minute: Int,
        interval: Int,
        timeZone: TimeZone,
        currentTime: Instant,
    ): Long {
        val daysDiff = (today.toEpochDays() - startDate.toEpochDays()).toInt()

        // 오늘이 알람 날짜라면
        if (daysDiff >= 0 && daysDiff % interval == 0) {
            val todayAlarmTime = today.atTime(hour, minute)
            val todayAlarmInstant = todayAlarmTime.toInstant(timeZone)
            if (currentTime < todayAlarmInstant) {
                return todayAlarmInstant.toEpochMilliseconds()
            }
        }

        // 다음 알람 날짜 찾기
        val nextAlarmDaysDiff = if (daysDiff < 0) {
            0
        } else {
            ((daysDiff / interval) + 1) * interval
        }

        val nextAlarmDate = startDate.plus(nextAlarmDaysDiff, DateTimeUnit.DAY)
        val nextAlarmTime = nextAlarmDate.atTime(hour, minute)
        return nextAlarmTime.toInstant(timeZone).toEpochMilliseconds()
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