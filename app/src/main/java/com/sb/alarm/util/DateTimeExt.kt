package com.sb.alarm.util

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/**
 * java.time.DayOfWeek를 kotlinx.datetime.DayOfWeek로 변환하는 확장 함수
 */
fun java.time.DayOfWeek.toKotlinDayOfWeek(): DayOfWeek {
    return when (this) {
        java.time.DayOfWeek.MONDAY -> DayOfWeek.MONDAY
        java.time.DayOfWeek.TUESDAY -> DayOfWeek.TUESDAY
        java.time.DayOfWeek.WEDNESDAY -> DayOfWeek.WEDNESDAY
        java.time.DayOfWeek.THURSDAY -> DayOfWeek.THURSDAY
        java.time.DayOfWeek.FRIDAY -> DayOfWeek.FRIDAY
        java.time.DayOfWeek.SATURDAY -> DayOfWeek.SATURDAY
        java.time.DayOfWeek.SUNDAY -> DayOfWeek.SUNDAY
    }
}

/**
 * DayOfWeek를 한국어 요일로 변환하는 확장 함수
 */
fun DayOfWeek.toKoreanString(): String {
    return when (this) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }
}

/**
 * 월 번호를 한국어 월 이름으로 변환하는 확장 함수
 */
fun Int.toKoreanMonth(): String {
    return when (this) {
        1 -> "1월"
        2 -> "2월"
        3 -> "3월"
        4 -> "4월"
        5 -> "5월"
        6 -> "6월"
        7 -> "7월"
        8 -> "8월"
        9 -> "9월"
        10 -> "10월"
        11 -> "11월"
        12 -> "12월"
        else -> "${this}월"
    }
}

/**
 * LocalDate를 한국어 날짜 형식으로 포맷팅하는 확장 함수
 */
fun LocalDate.toKoreanDateString(): String {
    val monthName = this.monthNumber.toKoreanMonth()
    return "${this.year}년 $monthName ${this.dayOfMonth}일"
}

