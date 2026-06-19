package com.smartaccounts.app.ui.backup

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.smartaccounts.app.R
import com.smartaccounts.app.databinding.ActivityRestoreBinding
import com.smartaccounts.app.di.ServiceLocator
import kotlinx.coroutines.launch

class RestoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRestoreBinding

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { confirmRestore(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.buttonRestore.setOnClickListener {
            openDocumentLauncher.launch(arrayOf("application/json"))
        }
    }

    private fun confirmRestore(uri: Uri) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.title_restore)
            .setMessage(R.string.message_restore_warning)
            .setPositiveButton(R.string.action_ok) { _, _ -> restoreFrom(uri) }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun restoreFrom(uri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        binding.buttonRestore.isEnabled = false
        lifecycleScope.launch {
            val result = ServiceLocator.provideBackupRepository(applicationContext).restoreFrom(uri)
            binding.progressBar.visibility = View.GONE
            binding.buttonRestore.isEnabled = true
            val messageRes = if (result.isSuccess) R.string.message_restore_success else R.string.message_restore_failed
            Snackbar.make(binding.root, messageRes, Snackbar.LENGTH_LONG).show()
            if (result.isSuccess) finish()
        }
    }
}
