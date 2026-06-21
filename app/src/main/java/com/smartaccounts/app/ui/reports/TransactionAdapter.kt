package com.smartaccounts.app.ui.reports

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smartaccounts.app.R
import com.smartaccounts.app.data.local.model.TransactionWithAccountName
import com.smartaccounts.app.databinding.ItemTransactionRowBinding
import com.smartaccounts.app.domain.model.Currency
import com.smartaccounts.app.domain.model.TransactionType
import com.smartaccounts.app.domain.util.DateFormats
import com.smartaccounts.app.domain.util.MoneyFormatter

class TransactionAdapter(
    private val onRowClick: (TransactionWithAccountName) -> Unit
) : ListAdapter<TransactionWithAccountName, TransactionAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemTransactionRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemTransactionRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TransactionWithAccountName) {
            val isCredit = item.type == TransactionType.CREDIT
            val colorRes = if (isCredit) R.color.credit_green else R.color.debit_red
            val color = binding.root.context.getColor(colorRes)

            binding.imageArrow.setImageResource(if (isCredit) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down)
            binding.imageArrow.setColorFilter(color)
            binding.textAmount.text = MoneyFormatter.format(item.amount)
            binding.textAmount.setTextColor(color)
            binding.textAccountName.text = item.accountName
            binding.textDate.text = DateFormats.formatDisplay(item.date)

            if (item.details.isNullOrBlank()) {
                binding.textDetails.visibility = View.GONE
            } else {
                binding.textDetails.visibility = View.VISIBLE
                binding.textDetails.text = item.details
            }

            if (item.currency == Currency.LOCAL) {
                binding.textCurrencyBadge.visibility = View.GONE
            } else {
                binding.textCurrencyBadge.visibility = View.VISIBLE
                binding.textCurrencyBadge.text = binding.root.context.getString(item.currency.labelRes)
            }

            binding.root.setOnClickListener { onRowClick(item) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TransactionWithAccountName>() {
            override fun areItemsTheSame(oldItem: TransactionWithAccountName, newItem: TransactionWithAccountName) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: TransactionWithAccountName, newItem: TransactionWithAccountName) =
                oldItem == newItem
        }
    }
}
