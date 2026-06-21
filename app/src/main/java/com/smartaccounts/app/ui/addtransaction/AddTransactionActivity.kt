package com.smartaccounts.app.ui.addtransaction

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.smartaccounts.app.R
import com.smartaccounts.app.databinding.ActivityAddTransactionBinding
import com.smartaccounts.app.di.ServiceLocator
import com.smartaccounts.app.domain.model.AccountCategory
import com.smartaccounts.app.domain.model.Currency
import com.smartaccounts.app.domain.model.TransactionType
import com.smartaccounts.app.domain.util.DateFormats
import com.smartaccounts.app.ui.common.ComingSoonActivity
import kotlinx.coroutines.launch
import java.io.File

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransactionBinding
    private var selectedDate: Long = System.currentTimeMillis()
    private var photoUri: Uri? = null
    private val category: AccountCategory by lazy {
        intent.getStringExtra(EXTRA_CATEGORY)?.let { runCatching { AccountCategory.valueOf(it) }.getOrNull() }
            ?: AccountCategory.GENERAL
    }

    private val viewModel: AddTransactionViewModel by viewModels {
        AddTransactionViewModelFactory(ServiceLocator.provideLedgerRepository(applicationContext))
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            binding.imagePreview.visibility = View.VISIBLE
            binding.imagePreview.setImageURI(photoUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.inputDate.setText(DateFormats.formatDisplay(selectedDate))

        intent.getStringExtra(EXTRA_PRESET_ACCOUNT_NAME)?.let { binding.inputName.setText(it) }

        binding.layoutDate.setEndIconOnClickListener { showDatePicker() }
        binding.inputDate.setOnClickListener { showDatePicker() }
        binding.buttonAttachPhoto.setOnClickListener { launchCamera() }
        binding.buttonCredit.setOnClickListener { save(TransactionType.CREDIT) }
        binding.buttonDebit.setOnClickListener { save(TransactionType.DEBIT) }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.accountNames.collect { names ->
                    binding.inputName.setAdapter(
                        ArrayAdapter(this@AddTransactionActivity, android.R.layout.simple_dropdown_item_1line, names)
                    )
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventFlow.collect { event -> handleEvent(event) }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add_transaction, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export_pdf -> {
                ComingSoonActivity.start(this, getString(R.string.content_description_pdf), R.drawable.ic_pdf)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker().setSelection(selectedDate).build()
        picker.addOnPositiveButtonClickListener { selection ->
            selectedDate = selection
            binding.inputDate.setText(DateFormats.formatDisplay(selection))
        }
        picker.show(supportFragmentManager, "date_picker")
    }

    private fun launchCamera() {
        val dir = File(cacheDir, "receipts").apply { mkdirs() }
        val file = File(dir, "receipt_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(this, "com.smartaccounts.app.fileprovider", file)
        photoUri = uri
        takePictureLauncher.launch(uri)
    }

    private fun save(type: TransactionType) {
        val currency = when (binding.radioGroupCurrency.checkedRadioButtonId) {
            R.id.radioSar -> Currency.SAR
            R.id.radioUsd -> Currency.USD
            else -> Currency.LOCAL
        }
        binding.layoutName.error = null
        binding.layoutAmount.error = null
        viewModel.save(
            name = binding.inputName.text.toString(),
            amountText = binding.inputAmount.text.toString(),
            type = type,
            currency = currency,
            date = selectedDate,
            details = binding.inputDetails.text.toString(),
            photoUri = photoUri?.toString(),
            category = category
        )
    }

    private fun handleEvent(event: AddTransactionViewModel.Event) {
        when (event) {
            is AddTransactionViewModel.Event.Saved -> {
                Snackbar.make(binding.root, R.string.message_transaction_saved, Snackbar.LENGTH_SHORT).show()
                finish()
            }
            is AddTransactionViewModel.Event.Error -> {
                val message = when (event.field) {
                    AddTransactionViewModel.ErrorField.NAME -> {
                        binding.layoutName.error = getString(R.string.error_name_required)
                        R.string.error_name_required
                    }
                    AddTransactionViewModel.ErrorField.AMOUNT_REQUIRED -> {
                        binding.layoutAmount.error = getString(R.string.error_amount_required)
                        R.string.error_amount_required
                    }
                    AddTransactionViewModel.ErrorField.AMOUNT_INVALID -> {
                        binding.layoutAmount.error = getString(R.string.error_amount_invalid)
                        R.string.error_amount_invalid
                    }
                }
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val EXTRA_PRESET_ACCOUNT_NAME = "extra_preset_account_name"
        const val EXTRA_CATEGORY = "extra_category"
    }
}
