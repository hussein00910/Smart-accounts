package com.smartaccounts.app.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartaccounts.app.data.local.AppDatabase
import com.smartaccounts.app.domain.model.AccountCategory
import com.smartaccounts.app.domain.model.Currency
import com.smartaccounts.app.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LedgerRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: LedgerRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = LedgerRepositoryImpl(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `summary totals match the reference ledger screenshot`() = runBlocking {
        // علي حسين alone contributes 26,124 to "عليك"; the other five accounts sum to
        // 11,793 owed back to the owner ("لك") - verified against the reference screenshots.
        repository.addTransaction("علي حسين", 26124.0, TransactionType.CREDIT, Currency.LOCAL, 0L, null, null)
        repository.addTransaction("حساب 1", 5813.0, TransactionType.DEBIT, Currency.LOCAL, 0L, null, null)
        repository.addTransaction("حساب 2", 350.0, TransactionType.DEBIT, Currency.LOCAL, 0L, null, null)
        repository.addTransaction("حساب 3", 415.0, TransactionType.DEBIT, Currency.LOCAL, 0L, null, null)
        repository.addTransaction("حساب 4", 5115.0, TransactionType.DEBIT, Currency.LOCAL, 0L, null, null)
        repository.addTransaction("حساب 5", 100.0, TransactionType.DEBIT, Currency.LOCAL, 0L, null, null)

        val totals = repository.observeSummaryTotals().first()

        assertEquals(26124.0, totals.totalYouOwe, 0.001)
        assertEquals(11793.0, totals.totalOwedToYou, 0.001)
    }

    @Test
    fun `addTransaction reuses an existing account case-insensitively`() = runBlocking {
        repository.addTransaction("Ali", 100.0, TransactionType.CREDIT, Currency.LOCAL, 0L, null, null)
        repository.addTransaction("ali", 40.0, TransactionType.DEBIT, Currency.LOCAL, 0L, null, null)

        val accounts = repository.observeAccountsWithStats().first()

        assertEquals(1, accounts.size)
        assertEquals(2, accounts[0].transactionCount)
        assertEquals(60.0, accounts[0].netLocalBalance, 0.001)
    }

    @Test
    fun `observeAllSummaryTotals aggregates across every category`() = runBlocking {
        repository.addTransaction("عام 1", 100.0, TransactionType.CREDIT, Currency.LOCAL, 0L, null, null, AccountCategory.GENERAL)
        repository.addTransaction("عميل 1", 50.0, TransactionType.DEBIT, Currency.LOCAL, 0L, null, null, AccountCategory.CUSTOMER)
        repository.addTransaction("مورد 1", 30.0, TransactionType.CREDIT, Currency.LOCAL, 0L, null, null, AccountCategory.SUPPLIER)

        val general = repository.observeSummaryTotals(AccountCategory.GENERAL).first()
        val customer = repository.observeSummaryTotals(AccountCategory.CUSTOMER).first()
        val supplier = repository.observeSummaryTotals(AccountCategory.SUPPLIER).first()
        val all = repository.observeAllSummaryTotals().first()

        assertEquals(general.totalYouOwe + customer.totalYouOwe + supplier.totalYouOwe, all.totalYouOwe, 0.001)
        assertEquals(
            general.totalOwedToYou + customer.totalOwedToYou + supplier.totalOwedToYou,
            all.totalOwedToYou,
            0.001
        )
    }

    @Test
    fun `observeTransactionsReport supports null date range and account filters`() = runBlocking {
        repository.addTransaction("Ali", 100.0, TransactionType.CREDIT, Currency.LOCAL, 1_000L, null, null)
        repository.addTransaction("Sara", 200.0, TransactionType.DEBIT, Currency.LOCAL, 2_000L, null, null)

        val all = repository.observeTransactionsReport(null, null, null).first()
        assertEquals(2, all.size)

        val byDateRange = repository.observeTransactionsReport(1_500L, 3_000L, null).first()
        assertEquals(1, byDateRange.size)
        assertEquals("Sara", byDateRange[0].accountName)

        val aliAccountId = repository.getAllAccountOptions().first { it.name == "Ali" }.id
        val byAccount = repository.observeTransactionsReport(null, null, aliAccountId).first()
        assertEquals(1, byAccount.size)
        assertEquals("Ali", byAccount[0].accountName)
    }
}
