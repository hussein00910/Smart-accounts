package com.smartaccounts.app.ui.settings.currencies

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.smartaccounts.app.R
import com.smartaccounts.app.data.prefs.AppPreferences
import com.smartaccounts.app.databinding.ActivityCurrenciesBinding
import com.smartaccounts.app.databinding.DialogEditTextBinding

class CurrenciesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCurrenciesBinding
    private lateinit var adapter: CurrencyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCurrenciesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = CurrencyAdapter(buildRows()) { row -> showRenameDialog(row) }
        binding.recyclerViewCurrencies.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCurrencies.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.recyclerViewCurrencies.adapter = adapter
    }

    private fun buildRows(): List<CurrencyRow> {
        val localLabel = AppPreferences.getLocalCurrencyLabel(this, getString(R.string.currency_local))
        return listOf(
            CurrencyRow("LOCAL", localLabel, editable = true),
            CurrencyRow("SAR", getString(R.string.currency_sar), editable = false),
            CurrencyRow("USD", getString(R.string.currency_usd), editable = false)
        )
    }

    private fun showRenameDialog(row: CurrencyRow) {
        val dialogBinding = DialogEditTextBinding.inflate(layoutInflater)
        dialogBinding.inputText.setText(row.label)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.currencies_rename_local_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_save) { _, _ ->
                val newLabel = dialogBinding.inputText.text.toString().trim()
                if (newLabel.isNotEmpty()) {
                    AppPreferences.setLocalCurrencyLabel(this, newLabel)
                    adapter.submitList(buildRows())
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
}
