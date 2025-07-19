/*
 * SPDX-FileCopyrightText: 2025 Simone Esposito
 * SPDX-License-Identifier: Apache-2.0
 */

package com.esposito.batteryinfo.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.esposito.batteryinfo.databinding.ItemBatteryDetailBinding
import com.esposito.batteryinfo.ui.models.BatteryDetailItem

class BatteryDetailAdapter : ListAdapter<BatteryDetailItem, BatteryDetailAdapter.DetailViewHolder>(BatteryDetailDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val binding = ItemBatteryDetailBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DetailViewHolder(private val binding: ItemBatteryDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BatteryDetailItem) {
            binding.txtTitle.text = item.title
            binding.txtValue.text = item.getFormattedValue()
        }
    }

    private class BatteryDetailDiffCallback : DiffUtil.ItemCallback<BatteryDetailItem>() {
        override fun areItemsTheSame(oldItem: BatteryDetailItem, newItem: BatteryDetailItem): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: BatteryDetailItem, newItem: BatteryDetailItem): Boolean {
            return oldItem == newItem
        }
    }
}