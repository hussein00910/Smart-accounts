package com.smartaccounts.app.domain.util

/**
 * Arabic numeric keyboards commonly emit Eastern Arabic-Indic digits (٠١٢٣٤٥٦٧٨٩) and the Arabic
 * decimal separator (٫) instead of ASCII digits/'.'. Plain String.toDoubleOrNull() can't parse
 * those and silently returns null, so amount input must be normalized before parsing.
 */
object AmountInputParser {

    private val easternArabicIndicDigits = '٠'..'٩'

    fun parse(text: String): Double? {
        val normalized = buildString {
            for (char in text.trim()) {
                when {
                    char in easternArabicIndicDigits -> append(char - '٠')
                    char == '٫' -> append('.')
                    char == '،' || char == '٬' || char == ',' -> Unit
                    else -> append(char)
                }
            }
        }
        return normalized.toDoubleOrNull()
    }
}
