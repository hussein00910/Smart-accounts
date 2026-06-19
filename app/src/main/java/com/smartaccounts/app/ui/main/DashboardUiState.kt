package com.smartaccounts.app.ui.main

import com.smartaccounts.app.data.local.model.AccountWithStats

data class DashboardUiState(
    val accounts: List<AccountWithStats> = emptyList(),
    val totalYouOwe: Double = 0.0,
    val totalOwedToYou: Double = 0.0,
    val searchQuery: String = ""
)
