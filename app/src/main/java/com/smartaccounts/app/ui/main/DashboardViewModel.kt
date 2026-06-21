package com.smartaccounts.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smartaccounts.app.data.repository.LedgerRepository
import com.smartaccounts.app.domain.model.AccountCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(private val repository: LedgerRepository) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val category = MutableStateFlow(AccountCategory.GENERAL)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardUiState> = category.flatMapLatest { selectedCategory ->
        combine(
            repository.observeAccountsWithStats(selectedCategory),
            repository.observeSummaryTotals(selectedCategory),
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
                searchQuery = query,
                category = selectedCategory
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onCategorySelected(selected: AccountCategory) {
        category.value = selected
    }
}

class DashboardViewModelFactory(private val repository: LedgerRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = DashboardViewModel(repository) as T
}
