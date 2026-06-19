package com.smartaccounts.app.data.local

import androidx.room.TypeConverter
import com.smartaccounts.app.domain.model.Currency
import com.smartaccounts.app.domain.model.TransactionType

class Converters {
    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toCurrency(value: String): Currency = Currency.valueOf(value)

    @TypeConverter
    fun fromCurrency(value: Currency): String = value.name
}
