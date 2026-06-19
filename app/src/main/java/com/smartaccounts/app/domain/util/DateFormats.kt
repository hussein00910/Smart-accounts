package com.smartaccounts.app.domain.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormats {

    private val displayFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)

    fun formatDisplay(epochMillis: Long): String = displayFormat.format(Date(epochMillis))
}
