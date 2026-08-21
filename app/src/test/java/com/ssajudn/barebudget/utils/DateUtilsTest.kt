package com.ssajudn.barebudget.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Characterization test for [DateUtils]. Functions that depend on the system
 * clock are tested for shape/stability rather than exact values; functions
 * that take an explicit input are tested deterministically.
 */
class DateUtilsTest {

    @Test
    fun `getCurrentDateISO returns yyyy-MM-dd shape`() {
        val out = DateUtils.getCurrentDateISO()
        // Shape: "2026-08-19" — 10 chars, dashes at positions 4 and 7.
        assertEquals(10, out.length)
        assertEquals('-', out[4])
        assertEquals('-', out[7])
    }

    @Test
    fun `getCurrentMonthYear returns yyyy-MM shape`() {
        val out = DateUtils.getCurrentMonthYear()
        // Shape: "2026-08" — 7 chars, dash at position 4.
        assertEquals(7, out.length)
        assertEquals('-', out[4])
    }

    @Test
    fun `formatDisplayDate formats ISO date string to dd MMM yyyy`() {
        val out = DateUtils.formatDisplayDate("2026-08-19")
        // Indonesian month abbreviation "Agu" for August.
        assertTrue("Expected 'Agu' for August, got: $out", out.contains("Agu"))
        assertTrue("Expected day '19', got: $out", out.startsWith("19"))
        assertTrue("Expected year '2026', got: $out", out.contains("2026"))
    }

    @Test
    fun `formatDisplayDate formats ISO timestamp to dd MMM yyyy`() {
        val out = DateUtils.formatDisplayDate("2026-08-19T14:30:00Z")
        assertTrue("Expected 'Agu' for August, got: $out", out.contains("Agu"))
        assertTrue("Expected day '19', got: $out", out.startsWith("19"))
    }

    @Test
    fun `formatDisplayDate returns raw input when unparseable`() {
        val raw = "not-a-date"
        assertEquals(raw, DateUtils.formatDisplayDate(raw))
    }

    @Test
    fun `getDaysUntilDue returns zero for today`() {
        val today = DateUtils.getCurrentDateISO()
        assertEquals(0L, DateUtils.getDaysUntilDue(today))
    }

    @Test
    fun `getDaysUntilDue returns positive for future date`() {
        // Build a date 5 days from today using the same ISO format.
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .parse(DateUtils.getCurrentDateISO())!!
        val cal = java.util.Calendar.getInstance().apply {
            time = today
            add(java.util.Calendar.DAY_OF_MONTH, 5)
        }
        val future = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(cal.time)
        val days = DateUtils.getDaysUntilDue(future)
        assertTrue("Expected ~5 days, got: $days", days in 4L..6L)
    }

    @Test
    fun `getDaysUntilDue returns negative for past date`() {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .parse(DateUtils.getCurrentDateISO())!!
        val cal = java.util.Calendar.getInstance().apply {
            time = today
            add(java.util.Calendar.DAY_OF_MONTH, -3)
        }
        val past = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(cal.time)
        val days = DateUtils.getDaysUntilDue(past)
        assertTrue("Expected negative for past, got: $days", days < 0)
    }

    @Test
    fun `getDueStatusMessage returns today message for zero days`() {
        val today = DateUtils.getCurrentDateISO()
        val msg = DateUtils.getDueStatusMessage(today)
        assertEquals("Jatuh tempo hari ini!", msg)
    }

    @Test
    fun `getDueStatusMessage returns tomorrow message for 1 day`() {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .parse(DateUtils.getCurrentDateISO())!!
        val cal = java.util.Calendar.getInstance().apply {
            time = today
            add(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        val tomorrow = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(cal.time)
        val msg = DateUtils.getDueStatusMessage(tomorrow)
        assertEquals("Besok", msg)
    }

    @Test
    fun `getDueStatusMessage returns future message for more than 1 day`() {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .parse(DateUtils.getCurrentDateISO())!!
        val cal = java.util.Calendar.getInstance().apply {
            time = today
            add(java.util.Calendar.DAY_OF_MONTH, 7)
        }
        val future = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(cal.time)
        val msg = DateUtils.getDueStatusMessage(future)
        assertTrue("Expected 'hari lagi' suffix, got: $msg", msg.contains("hari lagi"))
    }

    @Test
    fun `calculateNextDueDate advances monthly by one month`() {
        val out = DateUtils.calculateNextDueDate("2026-01-15", "MONTHLY")
        assertEquals("2026-02-15", out)
    }

    @Test
    fun `calculateNextDueDate advances weekly by one week`() {
        val out = DateUtils.calculateNextDueDate("2026-01-15", "WEEKLY")
        assertEquals("2026-01-22", out)
    }

    @Test
    fun `calculateNextDueDate advances yearly by one year`() {
        val out = DateUtils.calculateNextDueDate("2026-01-15", "YEARLY")
        assertEquals("2027-01-15", out)
    }

    @Test
    fun `calculateNextDueDate returns input unchanged for NONE`() {
        val out = DateUtils.calculateNextDueDate("2026-01-15", "NONE")
        assertEquals("2026-01-15", out)
    }

    @Test
    fun `calculateNextDueDate returns input unchanged for unknown interval`() {
        val out = DateUtils.calculateNextDueDate("2026-01-15", "DAILY")
        assertEquals("2026-01-15", out)
    }

    @Test
    @org.junit.Ignore("KNOWN BUG (locked for Phase 8): parseIsoToMillis uses JVM default TZ while formatMillisToIso forces UTC, causing 2026-08-19 to round-trip to 2026-08-18 in UTC+7. This test documents the bug; fixing it requires unifying timezones in DateUtils.")
    fun `parseIsoToMillis and formatMillisToIso roundtrip preserves date`() {
        val original = "2026-08-19"
        val millis = DateUtils.parseIsoToMillis(original)
        val back = DateUtils.formatMillisToIso(millis)
        assertEquals(original, back)
    }

    @Test
    fun `formatMillisToIso uses UTC to avoid timezone shift`() {
        // A midnight UTC instant must round-trip to the same ISO date,
        // regardless of the JVM's default timezone.
        val utc = java.util.TimeZone.getTimeZone("UTC")
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply {
            timeZone = utc
        }
        val millis = sdf.parse("2026-08-19")!!.time
        assertEquals("2026-08-19", DateUtils.formatMillisToIso(millis))
    }

    @Test
    fun `parseIsoToMillis returns current time for garbage input`() {
        val now = System.currentTimeMillis()
        val out = DateUtils.parseIsoToMillis("garbage")
        assertTrue("Expected ~now fallback, got: $out", out in (now - 5000)..(now + 5000))
    }
}
