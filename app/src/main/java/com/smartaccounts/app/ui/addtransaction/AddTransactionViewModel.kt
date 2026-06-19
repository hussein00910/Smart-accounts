package com.smartaccounts.app.ui.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smartaccounts.app.data.repository.LedgerRepository
import com.smartaccounts.app.domain.model.Currency
import com.smartaccounts.app.domain.model.TransactionType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddTransactionViewModel(private val repository: LedgerRepository) : ViewModel() {

    private val _accountNames = MutableStateFlow<List<String>>(emptyList())
    val accountNames: StateFlow<List<String>> = _accountNames

    private val events = Channel<Event>(Channel.BUFFERED)
    val eventFlow = events.receiveAsFlow()

    init {
        viewModelScope.launch {
            _accountNames.value = repository.getAllAccountNames()
        }
    }

    fun save(
        name: String,
        amountText: String,
        type: TransactionType,
        currency: Currency,
        date: Long,
        details: String?,
        photoUri: String?
    ) {
        if (name.isBlank()) {
            events.trySend(Event.Error(ErrorField.NAME))
            return
        }
        if (amountText.isBlank()) {
            events.trySend(Event.Error(ErrorField.AMOUNT_REQUIRED))
            return
        }
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            events.trySend(Event.Error(ErrorField.AMOUNT_INVALID))
            return
        }
        viewModelScope.launch {
            repository.addTransaction(
                accountName = name.trim(),
                amount = amount,
                type = type,
                currency = currency,
                date = date,
                details = details?.takeIf { it.isNotBlank() },
                photoUri = photoUri
            )
            events.send(Event.Saved)
        }
    }

    sealed interface Event {
        data object Saved : Event
        data class Error(val field: ErrorField) : Event
    }

    enum class ErrorField { NAME, AMOUNT_REQUIRED, AMOUNT_INVALID }
}

class AddTransactionViewModelFactory(private val repository: LedgerRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AddTransactionViewModel(repository) as T
}
