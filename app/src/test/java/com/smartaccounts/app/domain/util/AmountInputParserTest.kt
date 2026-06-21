package com.smartaccounts.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmountInputParserTest {

    @Test
    fun `parses plain ASCII amounts`() {
        assertEquals(1234.5, AmountInputParser.parse("1234.5"))
        assertEquals(100.0, AmountInputParser.parse("100"))
    }

    @Test
    fun `parses Eastern Arabic-Indic digits`() {
        assertEquals(1234.0, AmountInputParser.parse("١٢٣٤"))
    }

    @Test
    fun `parses Eastern Arabic-Indic digits with Arabic decimal separator`() {
        assertEquals(1234.56, AmountInputParser.parse("١٢٣٤٫٥٦"))
    }

    @Test
    fun `strips Arabic thousands separators`() {
        assertEquals(1234567.0, AmountInputParser.parse("١٬٢٣٤٬٥٦٧"))
    }

    @Test
    fun `strips stray Latin thousands commas`() {
        assertEquals(1234567.0, AmountInputParser.parse("1,234,567"))
    }

    @Test
    fun `trims surrounding whitespace`() {
        assertEquals(50.0, AmountInputParser.parse("  50  "))
    }

    @Test
    fun `returns null for blank or non-numeric input`() {
        assertNull(AmountInputParser.parse(""))
        assertNull(AmountInputParser.parse("abc"))
    }
}
