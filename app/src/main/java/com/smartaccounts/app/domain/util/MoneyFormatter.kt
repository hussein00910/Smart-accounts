package com.smartaccounts.app.domain.util

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

/**
 * Arabic ICU locales render Eastern Arabic-Indic digits (٠١٢٣) by default, which would break
 * the reference app's plain Latin-digit look ("26,124"). Locale.US keeps digits/grouping Latin
 * regardless of the device's/app's display locale.
 */
object MoneyFormatter {

    private val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    fun format(amount: Double): String = numberFormat.format(abs(amount))

    /**
     * Matches the dashboard's mixed-sign display: creditor accounts (net > 0) show a leading
     * minus sign, debtor accounts (net <= 0) show a plain unsigned magnitude.
     */
    fun formatSigned(netBalance: Double): String =
        if (netBalance > 0) "-${format(netBalance)}" else format(netBalance)
}
