package com.smartaccounts.app.data.backup

import com.smartaccounts.app.domain.model.AccountCategory
import com.smartaccounts.app.domain.model.Currency
import com.smartaccounts.app.domain.model.TransactionType
import kotlinx.serialization.Serializable

@Serializable
data class BackupAccount(
    val id: Long,
    val name: String,
    val sortOrder: Int,
    val createdAt: Long,
    val category: AccountCategory = AccountCategory.GENERAL
)

@Serializable
data class BackupTransaction(
    val id: Long,
    val accountId: Long,
    val amount: Double,
    val type: TransactionType,
    val currency: Currency,
    val date: Long,
    val details: String?,
    val photoUri: String?,
    val createdAt: Long
)

@Serializable
data class BackupPayload(
    val version: Int = 1,
    val exportedAt: Long,
    val accounts: List<BackupAccount>,
    val transactions: List<BackupTransaction>
)
