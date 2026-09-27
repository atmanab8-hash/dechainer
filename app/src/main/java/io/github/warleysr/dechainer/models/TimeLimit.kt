package io.github.warleysr.dechainer.models

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * A daily usage cap. By default the same [dailyMinutes] apply every day; when [weeklyMinutes] is
 * set the cap varies by weekday instead (indexed by [DayOfWeek.ordinal], Monday = 0). A value of 0
 * means "no limit" for that day.
 */
data class TimeLimit(val dailyMinutes: Int = 0, val weeklyMinutes: List<Int>? = null) {
    val isPerDay: Boolean get() = weeklyMinutes != null

    val isSet: Boolean get() = weeklyMinutes?.any { it > 0 } ?: (dailyMinutes > 0)

    fun minutesFor(day: DayOfWeek): Int = weeklyMinutes?.getOrNull(day.ordinal) ?: dailyMinutes

    fun todayMinutes(): Int = minutesFor(LocalDate.now().dayOfWeek)

    companion object {
        val NONE = TimeLimit()

        fun daily(minutes: Int) = TimeLimit(dailyMinutes = minutes)

        fun perDay(minutes: List<Int>): TimeLimit {
            require(minutes.size == DayOfWeek.entries.size)
            return TimeLimit(weeklyMinutes = minutes)
        }
    }
}
