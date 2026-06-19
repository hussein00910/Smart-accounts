package com.smartaccounts.app.data.repository

import androidx.room.withTransaction
import com.smartaccounts.app.data.local.AppDatabase
import com.smartaccounts.app.data.local.entity.AccountEntity
import com.smartaccounts.app.data.local.entity.TransactionEntity
import com.smartaccounts.app.data.local.model.AccountWithStats
import com.smartaccounts.app.data.local.model.SummaryTotalsRow
import com.smartaccounts.app.domain.model.Currency
import com.smartaccounts.app.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

class LedgerRepositoryImpl(private val database: AppDatabase) : LedgerRepository {

    private val accountDao = database.accountDao()
    private val transactionDao = database.transactionDao()

    override fun observeAccountsWithStats(): Flow<List<AccountWithStats>> =
        accountDao.observeAccountsWithStats()

    override fun observeSummaryTotals(): Flow<SummaryTotalsRow> =
        accountDao.observeSummaryTotals()

    override suspend fun getAllAccountNames(): List<String> =
        accountDao.getAllAccountNames()

    override suspend fun addTransaction(
        accountName: String,
        amount: Double,
        type: TransactionType,
        currency: Currency,
        date: Long,
        details: String?,
        photoUri: String?
    ): Long = database.withTransaction {
        val trimmedName = accountName.trim()
        val accountId = accountDao.findByName(trimmedName)?.id
            ?: accountDao.insert(AccountEntity(name = trimmedName))

        transactionDao.insert(
            TransactionEntity(
                accountId = accountId,
                amount = amount,
                type = type,
                currency = currency,
                date = date,
                details = details,
                photoUri = photoUri
            )
        )
    }
}
