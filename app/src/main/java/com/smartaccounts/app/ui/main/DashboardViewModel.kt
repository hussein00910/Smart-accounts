package com.smartaccounts.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smartaccounts.app.data.repository.LedgerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(private val repository: LedgerRepository) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.observeAccountsWithStats(),
        repository.observeSummaryTotals(),
        searchQuery
    ) { accounts, totals, query ->
        val filtered = if (query.isBlank()) {
            accounts
        } else {
            accounts.filter { it.name.contains(query, ignoreCase = true) }
        }
        DashboardUiState(
            accounts = filtered,
            totalYouOwe = totals.totalYouOwe,
            totalOwedToYou = totals.totalOwedToYou,
            searchQuery = query
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }
}

class DashboardViewModelFactory(private val repository: LedgerRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = DashboardViewModel(repository) as T
}
