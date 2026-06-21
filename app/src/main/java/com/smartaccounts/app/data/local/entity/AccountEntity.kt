package com.smartaccounts.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smartaccounts.app.domain.model.AccountCategory

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val category: AccountCategory = AccountCategory.GENERAL
)
