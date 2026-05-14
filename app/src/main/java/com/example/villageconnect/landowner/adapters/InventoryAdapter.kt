package com.example.villageconnect.landowner.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import androidx.recyclerview.widget.RecyclerView
import com.example.villageconnect.R
import com.example.villageconnect.landowner.models.InventoryItem

class InventoryAdapter(
    private val items: List<InventoryItem>,
    private val onOrderClick: (InventoryItem) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.tvItemName)
        val category: TextView = view.findViewById(R.id.tvItemCategory)
        val price: TextView = view.findViewById(R.id.tvItemPrice)
        val orderButton: MaterialButton = view.findViewById(R.id.btnOrderItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventory, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.itemName.text = item.itemName
        holder.category.text = "Category: ${item.category ?: "General"}"
        holder.price.text = "৳${item.price}"

        holder.orderButton.setOnClickListener {
            onOrderClick(item)
        }
    }
}