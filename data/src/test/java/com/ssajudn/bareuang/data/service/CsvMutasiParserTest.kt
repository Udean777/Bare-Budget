package com.ssajudn.bareuang.data.service

import org.junit.Assert.*
import org.junit.Test

class CsvMutasiParserTest {
    private val parser = CsvMutasiParser()

    @Test fun `generic comma`() {
        val csv = "foo,bar,baz\n01/01/2026,Top Up GoPay,50000"
        val res = parser.parse(csv)
        assertEquals(1, res.size)
        assertEquals(50000L, res[0].amount)
        assertEquals("2026-01-01", res[0].date)
        assertEquals(com.ssajudn.bareuang.domain.model.TransactionType.INCOME, res[0].type)
    }

    @Test fun `generic negative amount is expense`() {
        val csv = "foo,bar,baz\n01/01/2026,Belanja,-50000"
        val result = parser.parse(csv).single()
        assertEquals(50000L, result.amount)
        assertEquals(com.ssajudn.bareuang.domain.model.TransactionType.EXPENSE, result.type)
    }

    @Test fun `BCA semicolon debit kredit`() {
        val csv = "Tanggal;Keterangan;Debit;Kredit\n02-01-2026;TRF E-BANKING;50000;0"
        val res = parser.parse(csv)
        assertEquals(1, res.size)
        assertEquals(50000L, res[0].amount)
    }

    @Test fun `Rp amount with dots`() {
        assertEquals(1250000L, parser.parseAmount("Rp 1.250.000"))
        assertEquals(1250000L, parser.parseAmount("1.250.000,00"))
    }

    @Test fun `SALDO skip`() {
        val csv = "Tanggal,Keterangan,Jumlah\n01/01/2026,SALDO AWAL,1000000\n02/01/2026,Belanja,50000"
        val (list, skipped) = parser.parseWithStats(csv)
        assertEquals(1, list.size)
        assertEquals(1, skipped)
    }

    @Test fun `date formats`() {
        assertEquals("2026-01-05", parser.parseDate("05/01/2026"))
        assertEquals("2026-01-05", parser.parseDate("2026-01-05"))
        assertEquals("2026-01-05", parser.parseDate("05-01-2026"))
    }

    @Test fun `quoted fields`() {
        val line = "\"01/01/2026\",\"Toko, Besar\",\"50.000\""
        val cols = parser.splitCsvLine(line, ",")
        assertEquals(3, cols.size)
        assertEquals("Toko, Besar", cols[1])
    }
}
