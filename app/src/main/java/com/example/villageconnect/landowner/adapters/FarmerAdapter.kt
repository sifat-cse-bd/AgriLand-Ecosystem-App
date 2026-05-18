package com.example.villageconnect.landowner.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.villageconnect.R
import com.example.villageconnect.landowner.models.FarmerItem
import com.google.android.material.button.MaterialButton

class FarmerAdapter(
    private val items: List<FarmerItem>,
    private val onCardClick: (FarmerItem) -> Unit,
    private val onButtonClick: (FarmerItem) -> Unit,
    private val filterType: String // "hire", "active", "completed", "cancelled", "history"
) : RecyclerView.Adapter<FarmerAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvFarmerName)
        val skills: TextView = view.findViewById(R.id.tvFarmerSkills)
        val wage: TextView = view.findViewById(R.id.tvFarmerWage)
        val btnRequest: MaterialButton = view.findViewById(R.id.btnRequest)
        val selectedDate: TextView = view.findViewById(R.id.tvSelectedDate)
        val tvHireIds: TextView = view.findViewById(R.id.tvHireIds)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_farmer, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val farmer = items[position]

        holder.name.text = farmer.name
        holder.skills.text = "Skills: ${farmer.skills ?: "Not added"}"
        holder.wage.text = "Daily wage: ৳${farmer.dailyWage ?: 0.0}"
        
        // Hide IDs in Hire tab as it's for general profiles, show in other tabs for specific requests
        if (filterType == "hire") {
            holder.tvHireIds.visibility = View.GONE
            holder.selectedDate.visibility = View.GONE
        } else {
            holder.tvHireIds.visibility = View.VISIBLE
            holder.selectedDate.visibility = View.VISIBLE
            val reqIdStr = if (farmer.hireRequestId != null) "R-${farmer.hireRequestId}" else "N/A"
            val workIdStr = if (farmer.hireWorkId != null) "W-${farmer.hireWorkId}" else "N/A"
            holder.tvHireIds.text = "Req: $reqIdStr | Work: $workIdStr"
        }

        // ---------- Dynamic Button + Status ----------
        when(filterType){
            "hire" -> {
                holder.btnRequest.visibility = View.VISIBLE
                holder.btnRequest.text = "Send Request"
            }
            "active" -> {
                holder.btnRequest.visibility = View.VISIBLE
                
                val statusDisplay = when {
                    farmer.workStatus == "On-Work" -> "🔴 On Work"
                    farmer.requestStatus == "Accepted" -> "✓ Accepted"
                    farmer.requestStatus == "Pending" -> "⏳ Pending"
                    else -> "Processing"
                }
                
                // Only show Cancel button if it's not already On-Work
                if (farmer.workStatus == "On-Work") {
                    holder.btnRequest.visibility = View.GONE
                } else {
                    holder.btnRequest.text = "Cancel"
                    holder.btnRequest.visibility = View.VISIBLE
                }
                
                holder.selectedDate.text = "$statusDisplay (${farmer.selectedDate ?: ""})"
            }
            "completed" -> {
                holder.btnRequest.visibility = View.GONE
                holder.selectedDate.text = "✅ Completed (${farmer.selectedDate ?: ""})"
            }
            "cancelled" -> {
                holder.btnRequest.visibility = View.GONE
                holder.selectedDate.text = "❌ Cancelled"
            }
            "history" -> {
                holder.btnRequest.visibility = View.GONE
                holder.selectedDate.text = "📅 ${farmer.selectedDate ?: "Past work"}"
            }
        }

        holder.btnRequest.setOnClickListener { onButtonClick(farmer) }
        holder.itemView.setOnClickListener { onCardClick(farmer) }
    }
}
