package com.example.villageconnect.landowner.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.villageconnect.R
import com.example.villageconnect.landowner.models.FarmerItem

class FarmerAdapter(
    private val items: List<FarmerItem>,
    private val onClick: (FarmerItem) -> Unit
) : RecyclerView.Adapter<FarmerAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvFarmerName)
        val skills: TextView = view.findViewById(R.id.tvFarmerSkills)
        val wage: TextView = view.findViewById(R.id.tvFarmerWage)
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

        holder.itemView.setOnClickListener {
            onClick(farmer)
        }
    }
}