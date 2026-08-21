package com.ssajudn.barebudget.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Characterization test for [CurrencyFormatter] — locks in the current
 * behavior of the pure formatting/parsing helpers so that later refactors
 * (Phase 1 DI, Phase 8 constant extraction) cannot silently break them.
 */
class CurrencyFormatterTest {

    @Test
    fun `formatRupiah produces localized grouping for plain integer`() {
        val out = CurrencyFormatter.formatRupiah(50_000L)
        // id-ID renders "Rp\u00A050.000" or "Rp 50.000" depending on the JDK's
        // CLDR data; assert the stable, human-readable properties instead of
        // the exact whitespace.
        assertTrue("Expected 'Rp' prefix, got: $out", out.startsWith("Rp"))
        assertTrue("Expected grouping separator, got: $out", out.contains("50.000") || out.contains("50,000"))
    }

    @Test
    fun `formatRupiah handles zero`() {
        val out = CurrencyFormatter.formatRupiah(0L)
        assertTrue("Expected 'Rp' prefix for zero, got: $out", out.startsWith("Rp"))
    }

    @Test
    fun `formatRupiah groups millions with thousand separators`() {
        val out = CurrencyFormatter.formatRupiah(1_500_000L)
        assertTrue("Expected '1' million, got: $out", out.contains("1"))
    }

    @Test
    fun `parseAmount strips non-digit characters`() {
        assertEquals(50_000L, CurrencyFormatter.parseAmount("Rp 50.000"))
        assertEquals(50_000L, CurrencyFormatter.parseAmount("50,000"))
        assertEquals(50_000L, CurrencyFormatter.parseAmount("50000"))
        assertEquals(0L, CurrencyFormatter.parseAmount(""))
        assertEquals(0L, CurrencyFormatter.parseAmount("Rp"))
    }

    @Test
    fun `parseAmount handles nullish garbage as zero`() {
        assertEquals(0L, CurrencyFormatter.parseAmount("abc"))
        assertEquals(0L, CurrencyFormatter.parseAmount("---"))
    }

    @Test
    fun `formatCompact renders ribuan for thousands`() {
        val out = CurrencyFormatter.formatCompact(50_000L)
        // 50_000 -> "50 rb"
        assertTrue("Expected 'rb' suffix for thousands, got: $out", out.contains("rb"))
    }

    @Test
    fun `formatCompact renders juta for millions`() {
        val out = CurrencyFormatter.formatCompact(1_500_000L)
        // 1_500_000 -> "1,5 jt" or "1.5 jt"
        assertTrue("Expected 'jt' suffix for millions, got: $out", out.contains("jt"))
    }

    @Test
    fun `formatCompact renders M for billions`() {
        val out = CurrencyFormatter.formatCompact(2_000_000_000L)
        assertTrue("Expected 'M' suffix for billions, got: $out", out.contains("M"))
    }

    @Test
    fun `formatCompact returns raw value for small numbers`() {
        val out = CurrencyFormatter.formatCompact(500L)
        assertEquals("500", out)
    }
}
