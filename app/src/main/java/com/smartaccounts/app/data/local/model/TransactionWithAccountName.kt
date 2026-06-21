package com.smartaccounts.app.data.local.model

import com.smartaccounts.app.domain.model.Currency
import com.smartaccounts.app.domain.model.TransactionType

/** Subset projection of [com.smartaccounts.app.data.local.entity.TransactionEntity] for report rows; omits photoUri/createdAt. */
data class TransactionWithAccountName(
    val id: Long,
    val accountId: Long,
    val accountName: String,
    val amount: Double,
    val type: TransactionType,
    val currency: Currency,
    val date: Long,
    val details: String?
)
