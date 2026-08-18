package com.ssajudn.barebudget.utils

import com.ssajudn.barebudget.data.model.TransactionCategory
import java.util.regex.Pattern

data class ParsedReceipt(
    val merchantName: String,
    val totalAmount: Long,
    val suggestedCategory: TransactionCategory,
    val rawText: String
)

object ReceiptParser {

    /**
     * Parses raw OCR string from receipt into structured data
     */
    fun parse(rawText: String): ParsedReceipt {
        val lines = rawText.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val merchantName = extractMerchant(lines)
        val totalAmount = extractTotalAmount(lines)
        val suggestedCategory = guessCategory(rawText, merchantName)

        return ParsedReceipt(
            merchantName = merchantName,
            totalAmount = totalAmount,
            suggestedCategory = suggestedCategory,
            rawText = rawText
        )
    }

    private fun extractMerchant(lines: List<String>): String {
        // Merchant is usually within the first 1-3 lines of receipt
        for (i in 0 until minOf(3, lines.size)) {
            val line = lines[i]
            // Skip common metadata words
            if (!line.matches(Regex("(?i).*(struk|receipt|tanggal|kasir|pos|selamat|welcome|nota).*"))) {
                if (line.length >= 3 && line.any { it.isLetter() }) {
                    return cleanMerchantName(line)
                }
            }
        }
        return lines.firstOrNull() ?: "Merchant"
    }

    private fun cleanMerchantName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9 .&'-]"), "").trim()
    }

    private fun extractTotalAmount(lines: List<String>): Long {
        // Regex patterns for total lines in Indonesian receipts
        val totalKeywords = listOf("TOTAL", "GRAND TOTAL", "JUMLAH", "BAYAR", "TAGIHAN", "HARGA JUAL", "NETTO", "DEBIT", "CASH")
        val amountsFound = mutableListOf<Long>()

        for (line in lines) {
            val upper = line.uppercase()
            val containsKeyword = totalKeywords.any { upper.contains(it) }

            if (containsKeyword) {
                // Extract numbers formatted like 50.000, 50,000, 50000, Rp 50.000
                val amount = extractAmountFromLine(line)
                if (amount > 0) {
                    amountsFound.add(amount)
                }
            }
        }

        // Return the largest amount found near total keywords, or fallback to largest number overall
        if (amountsFound.isNotEmpty()) {
            return amountsFound.maxOrNull() ?: 0L
        }

        // Fallback: search all lines for amounts >= 1000
        for (line in lines.reversed()) {
            val amount = extractAmountFromLine(line)
            if (amount in 1000..50000000) {
                return amount
            }
        }

        return 0L
    }

    private fun extractAmountFromLine(line: String): Long {
        // Find numbers with optional dot/comma separators e.g. "Rp 45.000" or "45.000" or "45000"
        val pattern = Pattern.compile("(?i)(?:rp\\.?[\\s]*)?([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,8})")
        val matcher = pattern.matcher(line)
        var maxAmount = 0L

        while (matcher.find()) {
            val match = matcher.group(1) ?: continue
            val cleanNumber = match.replace(".", "").replace(",", "")
            val parsed = cleanNumber.toLongOrNull() ?: 0L
            if (parsed > maxAmount) {
                maxAmount = parsed
            }
        }
        return maxAmount
    }

    private fun guessCategory(rawText: String, merchant: String): TransactionCategory {
        val lower = ("$rawText $merchant").lowercase()
        return when {
            lower.containsAny("indomaret", "alfamart", "supermarket", "hypermart", "transmart", "toko", "belanja", "mart") -> TransactionCategory.SHOPPING
            lower.containsAny("kopi", "cafe", "restoran", "bakso", "ayam", "mcdonald", "kfc", "hokben", "gofood", "grabfood", "nasi", "warung", "mie", "sate", "kitchen", "coffee", "tea", "boba") -> TransactionCategory.FOOD
            lower.containsAny("bensin", "spbu", "pertamina", "shell", "grab", "gojek", "parkir", "tol", "krl", "mrt", "ojek") -> TransactionCategory.TRANSPORT
            lower.containsAny("pln", "listrik", "pdam", "air", "wifi", "indihome", "telkomsel", "pulsa", "kuota", "xl", "tri", "smartfren") -> TransactionCategory.BILLS
            lower.containsAny("bioskop", "cinema", "xxi", "cgv", "game", "steam", "playstation", "karaoke", "timezone") -> TransactionCategory.ENTERTAINMENT
            lower.containsAny("arisan", "kondangan", "sumbangan", "donasi", "infaq") -> TransactionCategory.SOCIAL
            else -> TransactionCategory.FOOD // Default to food as most frequent
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it) }
    }
}
