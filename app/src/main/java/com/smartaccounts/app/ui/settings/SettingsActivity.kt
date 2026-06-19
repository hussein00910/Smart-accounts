package com.smartaccounts.app.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.smartaccounts.app.R
import com.smartaccounts.app.databinding.ActivitySettingsBinding
import com.smartaccounts.app.ui.common.ComingSoonActivity
import com.smartaccounts.app.ui.settings.currencies.CurrenciesActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val items = listOf(
            SettingsItem(ID_CURRENCIES, R.string.settings_currencies, R.drawable.ic_currency),
            SettingsItem(ID_SECURITY, R.string.settings_security, R.drawable.ic_security),
            SettingsItem(ID_AUTO_BACKUP, R.string.settings_auto_backup, R.drawable.ic_backup),
            SettingsItem(ID_REMINDERS, R.string.settings_reminders, R.drawable.ic_notifications),
            SettingsItem(ID_PRINTING, R.string.settings_printing, R.drawable.ic_pdf),
            SettingsItem(ID_APPEARANCE, R.string.settings_appearance, R.drawable.ic_appearance),
            SettingsItem(ID_FONTS, R.string.settings_fonts, R.drawable.ic_font),
            SettingsItem(ID_MANAGE_ACCOUNTS, R.string.settings_manage_accounts, R.drawable.ic_accounts),
            SettingsItem(ID_EXPORT_DATA, R.string.settings_export_data, R.drawable.ic_export),
            SettingsItem(ID_RESET_DATA, R.string.settings_reset_data, R.drawable.ic_reset)
        )

        binding.recyclerViewSettings.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewSettings.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.recyclerViewSettings.adapter = SettingsAdapter(items) { handleItemClick(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_language -> {
                toggleLanguage()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleLanguage() {
        val current = AppCompatDelegate.getApplicationLocales()
        val isEnglish = !current.isEmpty && current[0]?.language == "en"
        val newLocales = if (isEnglish) {
            LocaleListCompat.forLanguageTags("ar")
        } else {
            LocaleListCompat.forLanguageTags("en")
        }
        AppCompatDelegate.setApplicationLocales(newLocales)
    }

    private fun handleItemClick(item: SettingsItem) {
        when (item.id) {
            ID_CURRENCIES -> startActivity(Intent(this, CurrenciesActivity::class.java))
            else -> ComingSoonActivity.start(this, getString(item.titleRes), item.iconRes)
        }
    }

    companion object {
        private const val ID_CURRENCIES = 1
        private const val ID_SECURITY = 2
        private const val ID_AUTO_BACKUP = 3
        private const val ID_REMINDERS = 4
        private const val ID_PRINTING = 5
        private const val ID_APPEARANCE = 6
        private const val ID_FONTS = 7
        private const val ID_MANAGE_ACCOUNTS = 8
        private const val ID_EXPORT_DATA = 9
        private const val ID_RESET_DATA = 10
    }
}
