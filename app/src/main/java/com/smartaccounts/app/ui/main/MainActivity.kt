package com.smartaccounts.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.smartaccounts.app.R
import com.smartaccounts.app.data.local.model.AccountWithStats
import com.smartaccounts.app.data.prefs.AppPreferences
import com.smartaccounts.app.databinding.ActivityMainBinding
import com.smartaccounts.app.di.ServiceLocator
import com.smartaccounts.app.domain.util.MoneyFormatter
import com.smartaccounts.app.ui.addtransaction.AddTransactionActivity
import com.smartaccounts.app.ui.backup.BackupActivity
import com.smartaccounts.app.ui.backup.RestoreActivity
import com.smartaccounts.app.ui.common.ComingSoonActivity
import com.smartaccounts.app.ui.settings.SettingsActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory(ServiceLocator.provideLedgerRepository(applicationContext))
    }

    private val adapter = AccountAdapter(
        onRowClick = { openAddTransaction(it) },
        onQuickAddClick = { openAddTransaction(it) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.toolbar.setNavigationOnClickListener { binding.drawerLayout.open() }
        binding.recyclerViewAccounts.adapter = adapter

        binding.fabAdd.setOnClickListener { openAddTransaction(null) }

        binding.navigationView.setNavigationItemSelectedListener { item ->
            handleDrawerItem(item)
            binding.drawerLayout.close()
            true
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomSummary.textLocalCurrencyLabel.text =
            AppPreferences.getLocalCurrencyLabel(this, getString(R.string.currency_local))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.queryHint = getString(R.string.search_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.onSearchQueryChanged(newText.orEmpty())
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notifications -> {
                ComingSoonActivity.start(this, getString(R.string.action_notifications), R.drawable.ic_notifications)
                true
            }
            R.id.action_sort -> {
                ComingSoonActivity.start(this, getString(R.string.action_sort), R.drawable.ic_sort)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun render(state: DashboardUiState) {
        adapter.submitList(state.accounts)
        val isEmpty = state.accounts.isEmpty()
        binding.emptyStateContainer.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewAccounts.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.bottomSummary.textYouOwe.text = MoneyFormatter.format(state.totalYouOwe)
        binding.bottomSummary.textOwedToYou.text = MoneyFormatter.format(state.totalOwedToYou)
    }

    private fun openAddTransaction(account: AccountWithStats?) {
        val intent = Intent(this, AddTransactionActivity::class.java)
        account?.let { intent.putExtra(AddTransactionActivity.EXTRA_PRESET_ACCOUNT_NAME, it.name) }
        startActivity(intent)
    }

    private fun handleDrawerItem(item: MenuItem) {
        when (item.itemId) {
            R.id.drawer_add_transaction -> openAddTransaction(null)
            R.id.drawer_report_general -> openComingSoon(R.string.drawer_report_general, R.drawable.ic_report)
            R.id.drawer_report_by_date -> openComingSoon(R.string.drawer_report_by_date, R.drawable.ic_report)
            R.id.drawer_report_by_account -> openComingSoon(R.string.drawer_report_by_account, R.drawable.ic_report)
            R.id.drawer_report_debts -> openComingSoon(R.string.drawer_report_debts, R.drawable.ic_report)
            R.id.drawer_report_balances -> openComingSoon(R.string.drawer_report_balances, R.drawable.ic_report)
            R.id.drawer_auto_recurrence -> openComingSoon(R.string.drawer_auto_recurrence, R.drawable.ic_recurrence)
            R.id.drawer_backup -> startActivity(Intent(this, BackupActivity::class.java))
            R.id.drawer_restore -> startActivity(Intent(this, RestoreActivity::class.java))
            R.id.drawer_google_drive -> openComingSoon(R.string.drawer_google_drive, R.drawable.ic_drive)
            R.id.drawer_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.drawer_contact_support -> openComingSoon(R.string.drawer_contact_support, R.drawable.ic_support)
            R.id.drawer_about -> openComingSoon(R.string.drawer_about, R.drawable.ic_about)
            R.id.drawer_share -> shareApp()
            R.id.drawer_exit -> finishAffinity()
        }
    }

    private fun openComingSoon(titleRes: Int, iconRes: Int) {
        ComingSoonActivity.start(this, getString(titleRes), iconRes)
    }

    private fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getString(R.string.app_name))
        }
        startActivity(Intent.createChooser(intent, getString(R.string.drawer_share)))
    }
}
