package com.ssajudn.bareuang.ui.common

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle
import java.util.Locale

/** Formats ISO dates for display at the UI boundary using the active device locale. */
object DateFormatter {
    fun formatDisplayDate(rawDate: String, locale: Locale = Locale.getDefault()): String =
        try {
            LocalDate.parse(rawDate.substring(0, 10))
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
        } catch (_: DateTimeParseException) {
            rawDate
        } catch (_: RuntimeException) {
            rawDate
        }
}
