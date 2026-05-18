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

        // ---------- Dynamic Button + Status ----------
        when(filterType){
            "hire" -> {
                holder.btnRequest.visibility = View.VISIBLE
                holder.btnRequest.text = if(farmer.hasPendingRequest) "Cancel Request" else "Select Day"
                holder.selectedDate.visibility = View.VISIBLE
                holder.selectedDate.text = farmer.selectedDate ?: "No date selected"
            }
            "active" -> {
                holder.btnRequest.visibility = View.VISIBLE
                holder.btnRequest.text = "Cancel"
                holder.selectedDate.visibility = View.VISIBLE
                holder.selectedDate.text = "Status: ${farmer.workStatus ?: "Pending"}"
            }
            "completed" -> {
                holder.btnRequest.visibility = View.GONE
                holder.selectedDate.visibility = View.VISIBLE
                holder.selectedDate.text = "Completed"
            }
            "cancelled" -> {
                holder.btnRequest.visibility = View.GONE
                holder.selectedDate.visibility = View.VISIBLE
                holder.selectedDate.text = "Cancelled"
            }
            "history" -> {
                holder.btnRequest.visibility = View.GONE
                holder.selectedDate.visibility = View.VISIBLE
                holder.selectedDate.text = farmer.selectedDate ?: "Past work"
            }
        }

        holder.btnRequest.setOnClickListener { onButtonClick(farmer) }
        holder.itemView.setOnClickListener { onCardClick(farmer) }
    }
}