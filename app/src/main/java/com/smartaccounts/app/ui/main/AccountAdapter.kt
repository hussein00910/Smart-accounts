package com.smartaccounts.app.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smartaccounts.app.R
import com.smartaccounts.app.data.local.model.AccountWithStats
import com.smartaccounts.app.databinding.ItemAccountRowBinding
import com.smartaccounts.app.domain.util.MoneyFormatter

class AccountAdapter(
    private val onRowClick: (AccountWithStats) -> Unit,
    private val onQuickAddClick: (AccountWithStats) -> Unit
) : ListAdapter<AccountWithStats, AccountAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemAccountRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAccountRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AccountWithStats) {
            val isCredit = item.netLocalBalance > 0
            val colorRes = if (isCredit) R.color.credit_green else R.color.debit_red
            val color = binding.root.context.getColor(colorRes)

            binding.imageArrow.setImageResource(if (isCredit) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down)
            binding.imageArrow.setColorFilter(color)
            binding.textAmount.text = MoneyFormatter.formatSigned(item.netLocalBalance)
            binding.textAmount.setTextColor(color)
            binding.textName.text = item.name
            binding.textCount.text = item.transactionCount.toString()
            binding.root.setOnClickListener { onRowClick(item) }
            binding.buttonQuickAdd.setOnClickListener { onQuickAddClick(item) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AccountWithStats>() {
            override fun areItemsTheSame(oldItem: AccountWithStats, newItem: AccountWithStats) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: AccountWithStats, newItem: AccountWithStats) =
                oldItem == newItem
        }
    }
}
