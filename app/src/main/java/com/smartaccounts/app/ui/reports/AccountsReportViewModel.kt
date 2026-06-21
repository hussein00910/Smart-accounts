package com.smartaccounts.app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smartaccounts.app.data.local.model.AccountWithStats
import com.smartaccounts.app.data.repository.LedgerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

enum class AccountsReportMode { DEBTS, BALANCES }

data class AccountsReportUiState(
    val accounts: List<AccountWithStats> = emptyList(),
    val totalYouOwe: Double = 0.0,
    val totalOwedToYou: Double = 0.0
)

class AccountsReportViewModel(
    private val repository: LedgerRepository,
    mode: AccountsReportMode
) : ViewModel() {

    private val modeFlow = MutableStateFlow(mode)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AccountsReportUiState> = modeFlow.flatMapLatest { selectedMode ->
        combine(
            repository.observeAllAccountsWithStats(),
            repository.observeAllSummaryTotals()
        ) { accounts, totals ->
            val filtered = if (selectedMode == AccountsReportMode.DEBTS) {
                accounts.filter { it.netLocalBalance != 0.0 }
            } else {
                accounts
            }
            AccountsReportUiState(
                accounts = filtered,
                totalYouOwe = totals.totalYouOwe,
                totalOwedToYou = totals.totalOwedToYou
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AccountsReportUiState())
}

class AccountsReportViewModelFactory(
    private val repository: LedgerRepository,
    private val mode: AccountsReportMode
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AccountsReportViewModel(repository, mode) as T
}
