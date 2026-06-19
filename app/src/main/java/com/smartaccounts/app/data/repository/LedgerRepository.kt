package com.smartaccounts.app.data.repository

import com.smartaccounts.app.data.local.model.AccountWithStats
import com.smartaccounts.app.data.local.model.SummaryTotalsRow
import com.smartaccounts.app.domain.model.AccountCategory
import com.smartaccounts.app.domain.model.Currency
import com.smartaccounts.app.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface LedgerRepository {

    fun observeAccountsWithStats(category: AccountCategory = AccountCategory.GENERAL): Flow<List<AccountWithStats>>

    fun observeSummaryTotals(category: AccountCategory = AccountCategory.GENERAL): Flow<SummaryTotalsRow>

    suspend fun getAllAccountNames(): List<String>

    suspend fun addTransaction(
        accountName: String,
        amount: Double,
        type: TransactionType,
        currency: Currency,
        date: Long,
        details: String?,
        photoUri: String?,
        category: AccountCategory = AccountCategory.GENERAL
    ): Long
}
