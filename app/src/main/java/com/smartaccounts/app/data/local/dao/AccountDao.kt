package com.smartaccounts.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.smartaccounts.app.data.local.entity.AccountEntity
import com.smartaccounts.app.data.local.model.AccountWithStats
import com.smartaccounts.app.data.local.model.SummaryTotalsRow
import com.smartaccounts.app.domain.model.AccountCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Insert
    suspend fun insert(account: AccountEntity): Long

    @Insert
    suspend fun insertAll(accounts: List<AccountEntity>)

    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): AccountEntity?

    @Query("SELECT * FROM accounts ORDER BY name COLLATE NOCASE")
    suspend fun getAllAccounts(): List<AccountEntity>

    @Query("SELECT name FROM accounts ORDER BY name COLLATE NOCASE")
    suspend fun getAllAccountNames(): List<String>

    @Query("SELECT * FROM accounts ORDER BY name COLLATE NOCASE")
    fun observeAllAccounts(): Flow<List<AccountEntity>>

    /**
     * Per-account aggregation: net local-currency balance = CREDIT("له") minus DEBIT("عليه"),
     * counting only currency = 'LOCAL'. Drives every row on the dashboard.
     */
    @Query(
        """
        SELECT
          a.id            AS id,
          a.name          AS name,
          a.sortOrder     AS sortOrder,
          COUNT(t.id)     AS transactionCount,
          COALESCE(SUM(
            CASE
              WHEN t.currency = 'LOCAL' AND t.type = 'CREDIT' THEN t.amount
              WHEN t.currency = 'LOCAL' AND t.type = 'DEBIT'  THEN -t.amount
              ELSE 0
            END
          ), 0.0)         AS netLocalBalance
        FROM accounts a
        LEFT JOIN transactions t ON t.accountId = a.id
        WHERE a.category = :category
        GROUP BY a.id
        ORDER BY a.name COLLATE NOCASE
        """
    )
    fun observeAccountsWithStats(category: AccountCategory): Flow<List<AccountWithStats>>

    /**
     * "عليك" (you owe) = sum of all positive (creditor) per-account balances.
     * "لك" (you're owed) = sum of all negative (debtor) per-account balances, as a magnitude.
     */
    @Query(
        """
        SELECT
          COALESCE(SUM(CASE WHEN net > 0 THEN net ELSE 0 END), 0.0)  AS totalYouOwe,
          COALESCE(SUM(CASE WHEN net < 0 THEN -net ELSE 0 END), 0.0) AS totalOwedToYou
        FROM (
          SELECT a.id,
            COALESCE(SUM(
              CASE
                WHEN t.currency = 'LOCAL' AND t.type = 'CREDIT' THEN t.amount
                WHEN t.currency = 'LOCAL' AND t.type = 'DEBIT'  THEN -t.amount
                ELSE 0
              END
            ), 0.0) AS net
          FROM accounts a
          LEFT JOIN transactions t ON t.accountId = a.id
          WHERE a.category = :category
          GROUP BY a.id
        )
        """
    )
    fun observeSummaryTotals(category: AccountCategory): Flow<SummaryTotalsRow>
}
