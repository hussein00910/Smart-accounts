package com.smartaccounts.app.domain.model

import com.smartaccounts.app.R
import kotlinx.serialization.Serializable

@Serializable
enum class Currency(val labelRes: Int) {
    LOCAL(R.string.currency_local),
    SAR(R.string.currency_sar),
    USD(R.string.currency_usd)
}
