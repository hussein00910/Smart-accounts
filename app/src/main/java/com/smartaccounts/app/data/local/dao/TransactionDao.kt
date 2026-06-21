package com.smartaccounts.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.smartaccounts.app.data.local.entity.TransactionEntity
import com.smartaccounts.app.data.local.model.TransactionWithAccountName
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC")
    fun observeForAccount(accountId: Long): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT
          t.id        AS id,
          t.accountId AS accountId,
          a.name      AS accountName,
          t.amount    AS amount,
          t.type      AS type,
          t.currency  AS currency,
          t.date      AS date,
          t.details   AS details
        FROM transactions t
        INNER JOIN accounts a ON a.id = t.accountId
        WHERE (:startDate IS NULL OR t.date >= :startDate)
          AND (:endDate IS NULL OR t.date <= :endDate)
          AND (:accountId IS NULL OR t.accountId = :accountId)
        ORDER BY t.date DESC
        """
    )
    fun observeTransactionsReport(
        startDate: Long?,
        endDate: Long?,
        accountId: Long?
    ): Flow<List<TransactionWithAccountName>>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllForExport(): List<TransactionEntity>

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
