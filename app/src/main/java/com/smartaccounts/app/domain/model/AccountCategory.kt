package com.smartaccounts.app.domain.model

import com.smartaccounts.app.R
import kotlinx.serialization.Serializable

@Serializable
enum class AccountCategory(val labelRes: Int) {
    GENERAL(R.string.category_general),
    CUSTOMER(R.string.category_customer),
    SUPPLIER(R.string.category_supplier)
}
