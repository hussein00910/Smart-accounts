package com.smartaccounts.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.smartaccounts.app.data.local.dao.AccountDao
import com.smartaccounts.app.data.local.dao.TransactionDao
import com.smartaccounts.app.data.local.entity.AccountEntity
import com.smartaccounts.app.data.local.entity.TransactionEntity

@Database(
    entities = [AccountEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_accounts.db"
                ).build().also { INSTANCE = it }
            }
    }
}
