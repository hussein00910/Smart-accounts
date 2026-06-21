package com.smartaccounts.app.data.repository

import androidx.room.withTransaction
import com.smartaccounts.app.data.local.AppDatabase
import com.smartaccounts.app.data.local.entity.AccountEntity
import com.smartaccounts.app.data.local.entity.TransactionEntity
import com.smartaccounts.app.data.local.model.AccountOption
import com.smartaccounts.app.data.local.model.AccountWithStats
import com.smartaccounts.app.data.local.model.SummaryTotalsRow
import com.smartaccounts.app.data.local.model.TransactionWithAccountName
import com.smartaccounts.app.domain.model.AccountCategory
import com.smartaccounts.app.domain.model.Currency
import com.smartaccounts.app.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

class LedgerRepositoryImpl(private val database: AppDatabase) : LedgerRepository {

    private val accountDao = database.accountDao()
    private val transactionDao = database.transactionDao()

    override fun observeAccountsWithStats(category: AccountCategory): Flow<List<AccountWithStats>> =
        accountDao.observeAccountsWithStats(category)

    override fun observeSummaryTotals(category: AccountCategory): Flow<SummaryTotalsRow> =
        accountDao.observeSummaryTotals(category)

    override fun observeAllAccountsWithStats(): Flow<List<AccountWithStats>> =
        accountDao.observeAllAccountsWithStats()

    override fun observeAllSummaryTotals(): Flow<SummaryTotalsRow> =
        accountDao.observeAllSummaryTotals()

    override fun observeTransactionsReport(
        startDate: Long?,
        endDate: Long?,
        accountId: Long?
    ): Flow<List<TransactionWithAccountName>> =
        transactionDao.observeTransactionsReport(startDate, endDate, accountId)

    override suspend fun getAllAccountNames(): List<String> =
        accountDao.getAllAccountNames()

    override suspend fun getAllAccountOptions(): List<AccountOption> =
        accountDao.getAllAccountOptions()

    override suspend fun addTransaction(
        accountName: String,
        amount: Double,
        type: TransactionType,
        currency: Currency,
        date: Long,
        details: String?,
        photoUri: String?,
        category: AccountCategory
    ): Long = database.withTransaction {
        val trimmedName = accountName.trim()
        val accountId = accountDao.findByName(trimmedName)?.id
            ?: accountDao.insert(AccountEntity(name = trimmedName, category = category))

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
