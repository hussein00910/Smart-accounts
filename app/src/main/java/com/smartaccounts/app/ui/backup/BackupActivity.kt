package com.smartaccounts.app.ui.backup

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.smartaccounts.app.R
import com.smartaccounts.app.databinding.ActivityBackupBinding
import com.smartaccounts.app.di.ServiceLocator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBackupBinding

    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { exportTo(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.buttonExport.setOnClickListener {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            createDocumentLauncher.launch("smart_accounts_backup_$timestamp.json")
        }
    }

    private fun exportTo(uri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        binding.buttonExport.isEnabled = false
        lifecycleScope.launch {
            val result = ServiceLocator.provideBackupRepository(applicationContext).exportTo(uri)
            binding.progressBar.visibility = View.GONE
            binding.buttonExport.isEnabled = true
            val messageRes = if (result.isSuccess) R.string.message_backup_success else R.string.message_backup_failed
            Snackbar.make(binding.root, messageRes, Snackbar.LENGTH_LONG).show()
        }
    }
}
