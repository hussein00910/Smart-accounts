package com.smartaccounts.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatterTest {

    @Test
    fun `format renders absolute value with Latin digit grouping`() {
        assertEquals("26,124", MoneyFormatter.format(26124.0))
        assertEquals("26,124", MoneyFormatter.format(-26124.0))
    }

    @Test
    fun `formatSigned shows a leading minus for positive creditor balances`() {
        assertEquals("-26,124", MoneyFormatter.formatSigned(26124.0))
    }

    @Test
    fun `formatSigned shows a plain magnitude for non-positive debtor balances`() {
        assertEquals("5,813", MoneyFormatter.formatSigned(-5813.0))
        assertEquals("0", MoneyFormatter.formatSigned(0.0))
    }
}
