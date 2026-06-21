package com.smartaccounts.app.data.prefs

import android.content.Context

object AppPreferences {
    private const val PREFS_NAME = "smart_accounts_prefs"
    private const val KEY_LOCAL_CURRENCY_LABEL = "local_currency_label"

    fun getLocalCurrencyLabel(context: Context, default: String): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LOCAL_CURRENCY_LABEL, default) ?: default
    }

    fun setLocalCurrencyLabel(context: Context, label: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LOCAL_CURRENCY_LABEL, label)
            .apply()
    }
}
