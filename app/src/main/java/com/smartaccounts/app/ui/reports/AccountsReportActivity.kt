package com.smartaccounts.app.ui.reports

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.smartaccounts.app.R
import com.smartaccounts.app.data.local.model.AccountWithStats
import com.smartaccounts.app.data.prefs.AppPreferences
import com.smartaccounts.app.databinding.ActivityAccountsReportBinding
import com.smartaccounts.app.di.ServiceLocator
import com.smartaccounts.app.domain.util.MoneyFormatter
import com.smartaccounts.app.ui.main.AccountAdapter
import kotlinx.coroutines.launch

class AccountsReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountsReportBinding

    private val mode: AccountsReportMode by lazy {
        intent.getStringExtra(EXTRA_MODE)?.let { runCatching { AccountsReportMode.valueOf(it) }.getOrNull() }
            ?: AccountsReportMode.BALANCES
    }

    private val viewModel: AccountsReportViewModel by viewModels {
        AccountsReportViewModelFactory(ServiceLocator.provideLedgerRepository(applicationContext), mode)
    }

    private val adapter = AccountAdapter(
        onRowClick = { openAccountHistory(it) },
        onQuickAddClick = { openAccountHistory(it) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountsReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = getString(
            if (mode == AccountsReportMode.DEBTS) R.string.drawer_report_debts else R.string.drawer_report_balances
        )
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.recyclerViewAccounts.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.headerSummary.textLocalCurrencyLabel.text =
            AppPreferences.getLocalCurrencyLabel(this, getString(R.string.currency_local))
    }

    private fun render(state: AccountsReportUiState) {
        adapter.submitList(state.accounts)
        val isEmpty = state.accounts.isEmpty()
        binding.emptyStateContainer.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewAccounts.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.headerSummary.textYouOwe.text = MoneyFormatter.format(state.totalYouOwe)
        binding.headerSummary.textOwedToYou.text = MoneyFormatter.format(state.totalOwedToYou)
    }

    private fun openAccountHistory(account: AccountWithStats) {
        TransactionsReportActivity.start(this, TransactionsReportMode.BY_ACCOUNT, account.id, account.name)
    }

    companion object {
        private const val EXTRA_MODE = "extra_mode"

        fun start(context: Context, mode: AccountsReportMode) {
            val intent = Intent(context, AccountsReportActivity::class.java).apply {
                putExtra(EXTRA_MODE, mode.name)
            }
            context.startActivity(intent)
        }
    }
}
