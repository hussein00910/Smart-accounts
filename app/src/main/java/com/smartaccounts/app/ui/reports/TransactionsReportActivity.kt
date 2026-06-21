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
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.smartaccounts.app.R
import com.smartaccounts.app.data.local.model.AccountOption
import com.smartaccounts.app.data.repository.LedgerRepository
import com.smartaccounts.app.databinding.ActivityTransactionsReportBinding
import com.smartaccounts.app.di.ServiceLocator
import com.smartaccounts.app.domain.util.DateFormats
import com.smartaccounts.app.domain.util.MoneyFormatter
import kotlinx.coroutines.launch

class TransactionsReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionsReportBinding
    private var accountOptions: List<AccountOption> = emptyList()

    private val repository: LedgerRepository by lazy {
        ServiceLocator.provideLedgerRepository(applicationContext)
    }

    private val mode: TransactionsReportMode by lazy {
        intent.getStringExtra(EXTRA_MODE)?.let { runCatching { TransactionsReportMode.valueOf(it) }.getOrNull() }
            ?: TransactionsReportMode.GENERAL
    }

    private val presetAccountId: Long? by lazy {
        intent.getLongExtra(EXTRA_ACCOUNT_ID, -1L).takeIf { it != -1L }
    }

    private val presetAccountName: String? by lazy { intent.getStringExtra(EXTRA_ACCOUNT_NAME) }

    private val viewModel: TransactionsReportViewModel by viewModels {
        TransactionsReportViewModelFactory(repository, mode, presetAccountId, presetAccountName)
    }

    private val adapter = TransactionAdapter(onRowClick = {})

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionsReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = getString(titleRes())
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.recyclerViewTransactions.adapter = adapter

        setUpFilterButton()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun titleRes(): Int = when (mode) {
        TransactionsReportMode.GENERAL -> R.string.drawer_report_general
        TransactionsReportMode.BY_DATE -> R.string.drawer_report_by_date
        TransactionsReportMode.BY_ACCOUNT -> R.string.drawer_report_by_account
    }

    private fun setUpFilterButton() {
        when (mode) {
            TransactionsReportMode.GENERAL -> binding.buttonFilter.visibility = View.GONE
            TransactionsReportMode.BY_DATE -> {
                binding.buttonFilter.visibility = View.VISIBLE
                binding.buttonFilter.setIconResource(R.drawable.ic_calendar)
                binding.buttonFilter.text = getString(R.string.report_filter_choose_date_range)
                binding.buttonFilter.setOnClickListener { showDateRangePicker() }
            }
            TransactionsReportMode.BY_ACCOUNT -> {
                binding.buttonFilter.visibility = View.VISIBLE
                binding.buttonFilter.setIconResource(R.drawable.ic_accounts)
                binding.buttonFilter.text = presetAccountName ?: getString(R.string.report_filter_choose_account)
                binding.buttonFilter.setOnClickListener { showAccountPicker() }
            }
        }
    }

    private fun showDateRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker().build()
        picker.addOnPositiveButtonClickListener { selection ->
            val start = selection?.first
            val end = selection?.second
            if (start != null && end != null) {
                viewModel.onDateRangeSelected(start, end)
                binding.buttonFilter.text = getString(
                    R.string.report_date_range_display,
                    DateFormats.formatDisplay(start),
                    DateFormats.formatDisplay(end)
                )
            }
        }
        picker.show(supportFragmentManager, "date_range_picker")
    }

    private fun showAccountPicker() {
        lifecycleScope.launch {
            if (accountOptions.isEmpty()) {
                accountOptions = repository.getAllAccountOptions()
            }
            val options = accountOptions
            val names = options.map { it.name }.toTypedArray()
            var selectedIndex = options.indexOfFirst { it.id == viewModel.uiState.value.accountId }
            MaterialAlertDialogBuilder(this@TransactionsReportActivity)
                .setTitle(R.string.report_filter_choose_account)
                .setSingleChoiceItems(names, selectedIndex) { _, index -> selectedIndex = index }
                .setPositiveButton(R.string.action_ok) { _, _ ->
                    if (selectedIndex in options.indices) {
                        val selected = options[selectedIndex]
                        viewModel.onAccountSelected(selected.id, selected.name)
                        binding.buttonFilter.text = selected.name
                    }
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
        }
    }

    private fun render(state: TransactionsReportUiState) {
        adapter.submitList(state.transactions)
        val isEmpty = state.transactions.isEmpty()
        binding.emptyStateContainer.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewTransactions.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.textTotalCredit.text = MoneyFormatter.format(state.totalCredit)
        binding.textTotalDebit.text = MoneyFormatter.format(state.totalDebit)
    }

    companion object {
        private const val EXTRA_MODE = "extra_mode"
        private const val EXTRA_ACCOUNT_ID = "extra_account_id"
        private const val EXTRA_ACCOUNT_NAME = "extra_account_name"

        fun start(
            context: Context,
            mode: TransactionsReportMode,
            presetAccountId: Long? = null,
            presetAccountName: String? = null
        ) {
            val intent = Intent(context, TransactionsReportActivity::class.java).apply {
                putExtra(EXTRA_MODE, mode.name)
                presetAccountId?.let { putExtra(EXTRA_ACCOUNT_ID, it) }
                presetAccountName?.let { putExtra(EXTRA_ACCOUNT_NAME, it) }
            }
            context.startActivity(intent)
        }
    }
}
