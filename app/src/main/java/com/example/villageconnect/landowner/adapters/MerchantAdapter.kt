package com.example.villageconnect.landowner.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.villageconnect.R
import com.example.villageconnect.landowner.models.MerchantItem

class MerchantAdapter(
    private val items: List<MerchantItem>,
    private val onClick: (MerchantItem) -> Unit
) : RecyclerView.Adapter<MerchantAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvMerchantName)
        val village: TextView = view.findViewById(R.id.tvMerchantVillage)
        val status: TextView = view.findViewById(R.id.tvMerchantStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_merchant, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val merchant = items[position]

        holder.name.text = merchant.name
        holder.village.text = merchant.village ?: "Village not updated"
        holder.status.text = "Available"

        holder.itemView.setOnClickListener {
            onClick(merchant)
        }
    }
}