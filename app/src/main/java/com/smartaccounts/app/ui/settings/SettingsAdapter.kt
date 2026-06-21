package com.smartaccounts.app.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.smartaccounts.app.databinding.ItemSettingsRowBinding

class SettingsAdapter(
    private val items: List<SettingsItem>,
    private val onItemClick: (SettingsItem) -> Unit
) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemSettingsRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemSettingsRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingsItem) {
            binding.imageIcon.setImageResource(item.iconRes)
            binding.imageIcon.setColorFilter(ContextCompat.getColor(binding.root.context, item.tintColorRes))
            binding.textTitle.setText(item.titleRes)
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
}
