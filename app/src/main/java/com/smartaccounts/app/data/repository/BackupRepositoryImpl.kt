package com.smartaccounts.app.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.smartaccounts.app.data.backup.BackupAccount
import com.smartaccounts.app.data.backup.BackupPayload
import com.smartaccounts.app.data.backup.BackupTransaction
import com.smartaccounts.app.data.backup.JsonBackupManager
import com.smartaccounts.app.data.local.AppDatabase
import com.smartaccounts.app.data.local.entity.AccountEntity
import com.smartaccounts.app.data.local.entity.TransactionEntity

class BackupRepositoryImpl(
    private val context: Context,
    private val database: AppDatabase,
    private val backupManager: JsonBackupManager
) : BackupRepository {

    override suspend fun exportTo(uri: Uri): Result<Unit> = runCatching {
        val accounts = database.accountDao().getAllAccounts()
        val transactions = database.transactionDao().getAllForExport()
        val payload = BackupPayload(
            exportedAt = System.currentTimeMillis(),
            accounts = accounts.map {
                BackupAccount(id = it.id, name = it.name, sortOrder = it.sortOrder, createdAt = it.createdAt)
            },
            transactions = transactions.map {
                BackupTransaction(
                    id = it.id,
                    accountId = it.accountId,
                    amount = it.amount,
                    type = it.type,
                    currency = it.currency,
                    date = it.date,
                    details = it.details,
                    photoUri = it.photoUri,
                    createdAt = it.createdAt
                )
            }
        )
        backupManager.writeToUri(context, uri, payload)
    }

    override suspend fun restoreFrom(uri: Uri): Result<Unit> = runCatching {
        val payload = backupManager.readFromUri(context, uri)
        database.withTransaction {
            database.clearAllTables()
            database.accountDao().insertAll(
                payload.accounts.map {
                    AccountEntity(id = it.id, name = it.name, sortOrder = it.sortOrder, createdAt = it.createdAt)
                }
            )
            database.transactionDao().insertAll(
                payload.transactions.map {
                    TransactionEntity(
                        id = it.id,
                        accountId = it.accountId,
                        amount = it.amount,
                        type = it.type,
                        currency = it.currency,
                        date = it.date,
                        details = it.details,
                        photoUri = it.photoUri,
                        createdAt = it.createdAt
                    )
                }
            )
        }
    }
}
