package com.example.villageconnect.landowner.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.villageconnect.R
import com.example.villageconnect.landowner.models.DashboardItem

class DashboardAdapter(
    private val items: List<DashboardItem>,
    private val onClick: (DashboardItem) -> Unit
) : RecyclerView.Adapter<DashboardAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: TextView = view.findViewById(R.id.tvDashboardIcon)
        val title: TextView = view.findViewById(R.id.tvDashboardTitle)
        val subtitle: TextView = view.findViewById(R.id.tvDashboardSubtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dashboard_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.icon.text = item.icon
        holder.title.text = item.title
        holder.subtitle.text = item.subtitle

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }
}