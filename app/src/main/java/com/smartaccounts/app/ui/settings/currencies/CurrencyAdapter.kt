package com.smartaccounts.app.ui.settings.currencies

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.smartaccounts.app.databinding.ItemCurrencyRowBinding

data class CurrencyRow(val code: String, val label: String, val editable: Boolean)

class CurrencyAdapter(
    private var items: List<CurrencyRow>,
    private val onEditClick: (CurrencyRow) -> Unit
) : RecyclerView.Adapter<CurrencyAdapter.ViewHolder>() {

    fun submitList(newItems: List<CurrencyRow>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemCurrencyRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemCurrencyRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CurrencyRow) {
            binding.textCode.text = item.code
            binding.textLabel.text = item.label
            binding.imageEdit.visibility = if (item.editable) View.VISIBLE else View.INVISIBLE
            binding.root.setOnClickListener(if (item.editable) View.OnClickListener { onEditClick(item) } else null)
        }
    }
}
