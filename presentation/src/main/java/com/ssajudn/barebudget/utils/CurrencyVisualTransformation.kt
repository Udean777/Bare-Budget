package com.ssajudn.barebudget.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Bulletproof CurrencyVisualTransformation for Indonesian Rupiah.
 * Strictly adheres to Jetpack Compose ValidatingOffsetMapping contract.
 */
class CurrencyVisualTransformation(
    private val prefix: String = "Rp "
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val formattedNumber = formatWithDots(originalText)
        val formattedFull = prefix + formattedNumber

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clampedOffset = offset.coerceIn(0, originalText.length)
                val rawSub = originalText.take(clampedOffset)
                val formattedSub = formatWithDots(rawSub)
                return (prefix.length + formattedSub.length).coerceIn(0, formattedFull.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= prefix.length) return 0
                val offsetWithoutPrefix = (offset - prefix.length).coerceAtLeast(0)
                val transformedSub = formattedFull.substring(prefix.length).take(offsetWithoutPrefix)
                val originalDigitsCount = transformedSub.count { it.isDigit() }
                return originalDigitsCount.coerceIn(0, originalText.length)
            }
        }

        return TransformedText(AnnotatedString(formattedFull), offsetMapping)
    }

    private fun formatWithDots(digits: String): String {
        if (digits.isEmpty()) return ""
        val length = digits.length
        val sb = StringBuilder()
        for (i in 0 until length) {
            sb.append(digits[i])
            val remaining = length - 1 - i
            if (remaining > 0 && remaining % 3 == 0) {
                sb.append('.')
            }
        }
        return sb.toString()
    }
}
