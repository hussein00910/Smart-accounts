package com.smartaccounts.app.domain.model

import kotlinx.serialization.Serializable

/**
 * CREDIT ("له", green/up-arrow) increases what the ledger owner owes the account.
 * DEBIT ("عليه", red/down-arrow) increases what the account owes the ledger owner.
 */
@Serializable
enum class TransactionType {
    CREDIT,
    DEBIT
}
