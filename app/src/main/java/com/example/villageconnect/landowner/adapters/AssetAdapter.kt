package com.example.villageconnect.landowner.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.villageconnect.R
import com.example.villageconnect.landowner.models.AssetItem

class AssetAdapter(
    private val items: List<AssetItem>,
    private val onClick: (AssetItem) -> Unit
) : RecyclerView.Adapter<AssetAdapter.ViewHolder>() {

    private var selectedPosition = -1

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val assetName: TextView = view.findViewById(R.id.tvHistoryTitle)
        val assetStatus: TextView = view.findViewById(R.id.tvHistorySubtitle)
        val assetCapacity: TextView = view.findViewById(R.id.tvHistoryStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val asset = items[position]

        holder.assetName.text = asset.assetType
        holder.assetStatus.text = "Status: ${asset.status}"
        holder.assetCapacity.text = "Capacity: ${asset.totalCapacity ?: 0.0}"

        holder.itemView.setOnClickListener {
            selectedPosition = position
            onClick(asset)
            notifyDataSetChanged()
        }
    }
}