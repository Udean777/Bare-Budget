package com.ssajudn.bareuang.domain.utils

import java.time.Instant
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Shared ISO date operations used at the domain/data/presentation boundaries.
 * Display-specific localization and clock injection remain follow-up work in
 * the dedicated time/localization refactor phases.
 */
object DateUtils {
    private val defaultClock: Clock = Clock.system(ZoneId.of("Asia/Jakarta"))

    fun getCurrentDateISO(clock: Clock = defaultClock): String = LocalDate.now(clock).toString()

    fun getCurrentMonthYear(clock: Clock = defaultClock): String = YearMonth.now(clock).toString()

    private fun parseLocalDate(isoDate: String): LocalDate =
        LocalDate.parse(isoDate.substring(0, 10))

    fun parseLocalDateOrNull(isoDate: String): LocalDate? =
        runCatching { parseLocalDate(isoDate) }.getOrNull()

    fun getDaysUntilDue(dueDateString: String, clock: Clock = defaultClock): Long =
        ChronoUnit.DAYS.between(LocalDate.now(clock), parseLocalDate(dueDateString))

    fun calculateNextDueDate(currentDueDateStr: String, interval: String): String {
        val date = parseLocalDate(currentDueDateStr)
        val next = when (interval.uppercase()) {
            "WEEKLY" -> date.plusWeeks(1)
            "MONTHLY" -> date.plusMonths(1)
            "YEARLY" -> date.plusYears(1)
            else -> return currentDueDateStr
        }
        return next.toString()
    }

    fun calculateNextWeeklyDay(fromDateStr: String, targetDayOfWeek: Int): String {
        val date = parseLocalDate(fromDateStr)
        var daysToAdd = (targetDayOfWeek - date.dayOfWeek.value + 7) % 7
        if (daysToAdd == 0) daysToAdd = 7
        return date.plusDays(daysToAdd.toLong()).toString()
    }

    fun calculateNextMonthlyDate(fromDateStr: String, targetDayOfMonth: Int): String {
        val nextMonth = parseLocalDate(fromDateStr).plusMonths(1)
        val maxDay = nextMonth.lengthOfMonth()
        return nextMonth.withDayOfMonth(targetDayOfMonth.coerceIn(1, maxDay)).toString()
    }

    fun getDayOfWeek(dateStr: String): Int = parseLocalDate(dateStr).dayOfWeek.value

    fun getDayOfMonth(dateStr: String): Int = parseLocalDate(dateStr).dayOfMonth

    fun parseIsoToMillis(isoDate: String): Long? =
        try {
            parseLocalDate(isoDate).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        } catch (_: Exception) {
            null
        }

    fun formatMillisToIso(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
}
