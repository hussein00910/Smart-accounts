package com.smartaccounts.app.di

import android.content.Context
import com.smartaccounts.app.data.backup.JsonBackupManager
import com.smartaccounts.app.data.local.AppDatabase
import com.smartaccounts.app.data.repository.BackupRepository
import com.smartaccounts.app.data.repository.BackupRepositoryImpl
import com.smartaccounts.app.data.repository.DriveSyncRepository
import com.smartaccounts.app.data.repository.DriveSyncRepositoryStub
import com.smartaccounts.app.data.repository.LedgerRepository
import com.smartaccounts.app.data.repository.LedgerRepositoryImpl

object ServiceLocator {

    @Volatile private var database: AppDatabase? = null
    @Volatile private var ledgerRepository: LedgerRepository? = null
    @Volatile private var backupRepository: BackupRepository? = null
    @Volatile private var driveSyncRepository: DriveSyncRepository? = null

    fun provideDatabase(context: Context): AppDatabase =
        database ?: synchronized(this) {
            database ?: AppDatabase.getInstance(context).also { database = it }
        }

    fun provideLedgerRepository(context: Context): LedgerRepository =
        ledgerRepository ?: synchronized(this) {
            ledgerRepository ?: LedgerRepositoryImpl(provideDatabase(context)).also { ledgerRepository = it }
        }

    fun provideBackupRepository(context: Context): BackupRepository =
        backupRepository ?: synchronized(this) {
            backupRepository ?: BackupRepositoryImpl(
                context.applicationContext,
                provideDatabase(context),
                JsonBackupManager()
            ).also { backupRepository = it }
        }

    fun provideDriveSyncRepository(): DriveSyncRepository =
        driveSyncRepository ?: synchronized(this) {
            driveSyncRepository ?: DriveSyncRepositoryStub().also { driveSyncRepository = it }
        }
}
