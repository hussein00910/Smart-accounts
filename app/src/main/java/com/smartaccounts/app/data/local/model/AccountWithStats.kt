package com.smartaccounts.app.data.local.model

data class AccountWithStats(
    val id: Long,
    val name: String,
    val sortOrder: Int,
    val transactionCount: Int,
    val netLocalBalance: Double
)

data class SummaryTotalsRow(
    val totalYouOwe: Double,
    val totalOwedToYou: Double
)
