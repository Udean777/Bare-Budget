package com.ssajudn.bareuang.data.service

import com.ssajudn.bareuang.domain.model.ImportDraft
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvMutasiParser @Inject constructor() : com.ssajudn.bareuang.domain.port.CsvParserPort {

    fun parse(csvText: String): List<ImportDraft> = parseWithStats(csvText).first

    override fun parseWithStats(csvText: String): Pair<List<ImportDraft>, Int> {
        // Guard: reject > 5MB or > 5000 lines (prevent OOM)
        if (csvText.length > 5 * 1024 * 1024) return emptyList<ImportDraft>() to 0
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return emptyList<ImportDraft>() to 0
        if (lines.size > 5000) return emptyList<ImportDraft>() to 0

        val delimiter = detectDelimiter(lines.first())
        val headerIndices = detectHeaderIndices(lines.first(), delimiter)
        val hasHeader = headerIndices != null
        val dataLines = if (hasHeader) lines.drop(1) else lines

        val drafts = mutableListOf<ImportDraft>()
        var skipped = 0
        for (rawLine in dataLines) {
            val draft = parseLine(rawLine, delimiter, headerIndices)
            if (draft == null) { skipped++; continue }
            if (draft.merchant.contains("SALDO AWAL", true) || draft.merchant.contains("SALDO AKHIR", true)) { skipped++; continue }
            drafts.add(draft)
        }
        return drafts to skipped
    }

    private fun detectDelimiter(header: String): String {
        val semi = header.count { it == ';' }
        val comma = header.count { it == ',' }
        return if (semi > comma) ";" else ","
    }

    private fun detectHeaderIndices(header: String, delimiter: String): Map<String, Int>? {
        val cols = splitCsvLine(header, delimiter).map { it.lowercase().trim() }
        val hasKnown = cols.any { it in setOf("tanggal", "date", "waktu", "keterangan", "description", "deskripsi", "merchant", "jumlah", "amount", "nominal", "mutasi", "debit", "kredit", "credit") }
        if (!hasKnown) return null
        val map = mutableMapOf<String, Int>()
        cols.forEachIndexed { idx, col ->
            when (col) {
                "tanggal", "date", "waktu", "tgl" -> map["date"] = idx
                "keterangan", "description", "deskripsi", "merchant", "detail", "uraian" -> map["merchant"] = idx
                "jumlah", "amount", "nominal", "mutasi", "nilai" -> map["amount"] = idx
                "debit", "debet" -> map["debit"] = idx
                "kredit", "credit", "kredit " -> map["credit"] = idx
                "tipe", "type", "jenis" -> map["type"] = idx
                "kategori", "category" -> map["category"] = idx
            }
        }
        return map
    }

    private fun parseLine(rawLine: String, delimiter: String, headerIndices: Map<String, Int>?): ImportDraft? {
        val cols = splitCsvLine(rawLine, delimiter)
        if (cols.isEmpty()) return null

        val dateStr: String
        val merchant: String
        val amount: Long
        val type: TransactionType
        val category: TransactionCategory

        if (headerIndices != null) {
            dateStr = parseDate(cols.getOrNull(headerIndices["date"] ?: -1) ?: "")
            merchant = cols.getOrNull(headerIndices["merchant"] ?: -1)?.trim()?.takeIf { it.isNotBlank() } ?: cols.firstOrNull()?.trim().orEmpty()

            // debit/kredit split columns
            val debitIdx = headerIndices["debit"]
            val creditIdx = headerIndices["credit"]
            if (debitIdx != null || creditIdx != null) {
                val debitVal = debitIdx?.let { cols.getOrNull(it) }?.let { parseAmount(it) } ?: 0L
                val creditVal = creditIdx?.let { cols.getOrNull(it) }?.let { parseAmount(it) } ?: 0L
                when {
                    debitVal > 0 -> { amount = debitVal; type = TransactionType.EXPENSE }
                    creditVal > 0 -> { amount = creditVal; type = TransactionType.INCOME }
                    else -> return null
                }
            } else {
                val amountIdx = headerIndices["amount"]
                val rawAmount = amountIdx?.let { cols.getOrNull(it) } ?: cols.lastOrNull().orEmpty()
                val parsed = parseAmount(rawAmount)
                if (parsed == 0L) return null
                // type column or sign
                val typeRaw = headerIndices["type"]?.let { cols.getOrNull(it) }?.lowercase()
                type = when {
                    typeRaw != null && (typeRaw.trim() == "in" || typeRaw == "income" || typeRaw == "pemasukan") || typeRaw?.contains("masuk") == true || typeRaw?.contains("credit") == true -> TransactionType.INCOME
                    typeRaw != null && (typeRaw.trim() == "out" || typeRaw == "expense") || typeRaw?.contains("keluar") == true || typeRaw?.contains("debit") == true -> TransactionType.EXPENSE
                    rawAmount.trim().startsWith("-") -> TransactionType.EXPENSE
                    else -> if (parsed < 0) TransactionType.EXPENSE else TransactionType.EXPENSE // default expense; caller can toggle
                }
                // handle negative sign amount
                amount = kotlin.math.abs(parsed)
                if (amount == 0L) return null
            }
            if (dateStr.isBlank()) return null
            category = guessCategory(merchant)
            return ImportDraft(UUID.randomUUID().toString(), amount, type, category, merchant.ifBlank { "Import" }, dateStr, rawLine)
        } else {
            // generic: try to find date and amount in cols
            var foundDate: String? = null
            var foundAmount: Long? = null
            var foundMerchant = ""
            for (c in cols) {
                if (foundDate == null) parseDateOrNull(c)?.let { foundDate = it }
                if (foundAmount == null && parseDateOrNull(c) == null) {
                    parseAmountOrNull(c)?.let { if (it > 0) foundAmount = it }
                }
            }
            if (foundDate == null || foundAmount == null) return null
            foundMerchant = cols.firstOrNull { it.length >= 3 && it.any { ch -> ch.isLetter() } && parseDateOrNull(it) == null && parseAmountOrNull(it) == null }?.trim() ?: cols.firstOrNull().orEmpty()
            category = guessCategory(foundMerchant)
            // check sign
            val rawAmountCol = cols.firstOrNull { parseDateOrNull(it) == null && parseAmountOrNull(it) != null } ?: ""
            val isNegative = rawAmountCol.trim().startsWith("-")
            type = if (isNegative) TransactionType.EXPENSE else TransactionType.INCOME
            return ImportDraft(UUID.randomUUID().toString(), kotlin.math.abs(foundAmount!!), type, category, foundMerchant.ifBlank { "Import" }, foundDate!!, rawLine)
        }
    }

    internal fun splitCsvLine(line: String, delimiter: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') { sb.append('"'); i++ }
                    else inQuotes = !inQuotes
                }
                !inQuotes && line.startsWith(delimiter, i) -> { result.add(sb.toString().trim()); sb.clear(); }
                else -> sb.append(c)
            }
            i++
        }
        result.add(sb.toString().trim())
        return result.map { it.removeSurrounding("\"").trim() }
    }

    internal fun parseAmount(raw: String): Long {
        if (raw.isBlank()) return 0L
        val s = raw.trim().replace("Rp", "", true).replace("IDR", "", true).trim()
        // handle negative
        val negative = s.startsWith("-")
        val cleaned = s.replace("-", "").trim()
        // keep digits, dots, commas
        val numeric = cleaned.filter { it.isDigit() || it == '.' || it == ',' }
        if (numeric.isEmpty()) return 0L
        // Indonesian: 1.250.000 or 1.250.000,00 -> last comma is decimal
        val withoutDecimal = if (numeric.contains(",")) {
            // if comma followed by 2 digits at end -> decimal, drop it
            val lastComma = numeric.lastIndexOf(',')
            val after = numeric.substring(lastComma + 1)
            if (after.length == 2 && after.all { it.isDigit() }) {
                numeric.substring(0, lastComma).replace(".", "").replace(",", "")
            } else {
                numeric.replace(".", "").replace(",", "")
            }
        } else {
            numeric.replace(".", "").replace(",", "")
        }
        val parsed = withoutDecimal.toLongOrNull() ?: 0L
        return if (negative) -parsed else parsed
    }

    private fun parseAmountOrNull(raw: String): Long? {
        val v = parseAmount(raw)
        return if (v != 0L) kotlin.math.abs(v) else null
    }

    internal fun parseDate(raw: String): String {
        return parseDateOrNull(raw) ?: ""
    }

    internal fun parseDateOrNull(raw: String): String? {
        val s = raw.trim().substringBefore(" ").trim()
        if (s.isBlank()) return null
        val patterns = listOf("dd/MM/yyyy", "d/M/yyyy", "dd-MM-yyyy", "d-M-yyyy", "yyyy-MM-dd", "dd.MM.yyyy", "dd MMM yyyy", "d MMM yyyy")
        for (pat in patterns) {
            try {
                val fmt = DateTimeFormatter.ofPattern(pat, java.util.Locale.ENGLISH)
                val d = LocalDate.parse(s, fmt)
                return d.toString() // yyyy-MM-dd
            } catch (_: Exception) { }
            // try Indonesian month names
            try {
                val fmtId = DateTimeFormatter.ofPattern(pat, java.util.Locale.forLanguageTag("id-ID"))
                val d = LocalDate.parse(s, fmtId)
                return d.toString()
            } catch (_: Exception) { }
        }
        // try yyyy-MM-dd with time
        try { return LocalDate.parse(s.take(10)).toString() } catch (_: Exception) {}
        return null
    }

    private fun guessCategory(merchant: String): TransactionCategory {
        val lower = merchant.lowercase()
        return when {
            lower.containsAny("indomaret", "alfamart", "supermarket", "hypermart", "toko", "mart", "belanja") -> TransactionCategory.SHOPPING
            lower.containsAny("kopi", "cafe", "restoran", "bakso", "ayam", "mcdonald", "kfc", "gofood", "grabfood", "nasi", "warung", "mie", "sate", "coffee") -> TransactionCategory.FOOD
            lower.containsAny("bensin", "spbu", "pertamina", "shell", "grab", "gojek", "parkir", "tol", "krl", "mrt", "ojek") -> TransactionCategory.TRANSPORT
            lower.containsAny("pln", "listrik", "pdam", "wifi", "indihome", "telkomsel", "pulsa", "kuota") -> TransactionCategory.BILLS
            lower.containsAny("bioskop", "cinema", "xxi", "cgv", "game", "steam", "playstation") -> TransactionCategory.ENTERTAINMENT
            lower.containsAny("gaji", "salary", "upah") -> TransactionCategory.SALARY
            else -> TransactionCategory.OTHER
        }
    }

    private fun String.containsAny(vararg keywords: String) = keywords.any { this.contains(it) }
}
