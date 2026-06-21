package com.smartaccounts.app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smartaccounts.app.data.local.model.TransactionWithAccountName
import com.smartaccounts.app.data.repository.LedgerRepository
import com.smartaccounts.app.domain.model.Currency
import com.smartaccounts.app.domain.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

enum class TransactionsReportMode { GENERAL, BY_DATE, BY_ACCOUNT }

private data class TransactionsReportFilter(
    val mode: TransactionsReportMode,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val accountId: Long? = null,
    val accountName: String? = null
)

data class TransactionsReportUiState(
    val mode: TransactionsReportMode = TransactionsReportMode.GENERAL,
    val transactions: List<TransactionWithAccountName> = emptyList(),
    val totalCredit: Double = 0.0,
    val totalDebit: Double = 0.0,
    val accountId: Long? = null,
    val accountName: String? = null
)

class TransactionsReportViewModel(
    private val repository: LedgerRepository,
    initialMode: TransactionsReportMode,
    initialAccountId: Long? = null,
    initialAccountName: String? = null
) : ViewModel() {

    private val filter = MutableStateFlow(
        TransactionsReportFilter(mode = initialMode, accountId = initialAccountId, accountName = initialAccountName)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TransactionsReportUiState> = filter.flatMapLatest { current ->
        repository.observeTransactionsReport(current.startDate, current.endDate, current.accountId).map { transactions ->
            TransactionsReportUiState(
                mode = current.mode,
                transactions = transactions,
                totalCredit = transactions
                    .filter { it.currency == Currency.LOCAL && it.type == TransactionType.CREDIT }
                    .sumOf { it.amount },
                totalDebit = transactions
                    .filter { it.currency == Currency.LOCAL && it.type == TransactionType.DEBIT }
                    .sumOf { it.amount },
                accountId = current.accountId,
                accountName = current.accountName
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionsReportUiState(mode = initialMode))

    fun onDateRangeSelected(startDate: Long, endDate: Long) {
        filter.value = filter.value.copy(startDate = startDate, endDate = endDate)
    }

    fun onAccountSelected(accountId: Long, accountName: String) {
        filter.value = filter.value.copy(accountId = accountId, accountName = accountName)
    }
}

class TransactionsReportViewModelFactory(
    private val repository: LedgerRepository,
    private val mode: TransactionsReportMode,
    private val initialAccountId: Long? = null,
    private val initialAccountName: String? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        TransactionsReportViewModel(repository, mode, initialAccountId, initialAccountName) as T
}
