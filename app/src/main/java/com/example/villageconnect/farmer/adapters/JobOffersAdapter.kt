package com.example.villageconnect.farmer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.villageconnect.R
import com.example.villageconnect.farmer.models.JobOfferItem

class JobOffersAdapter(
    private val items: List<JobOfferItem>,
    private val onAcceptClick: (JobOfferItem) -> Unit,
    private val onRejectClick: (JobOfferItem) -> Unit,
    private val onItemClick: (JobOfferItem) -> Unit
) : RecyclerView.Adapter<JobOffersAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvLandownerName)
        val requestDate: TextView = view.findViewById(R.id.tvRequestDate)
        val address: TextView = view.findViewById(R.id.tvAddress)
        val btnAccept: Button = view.findViewById(R.id.btnAccept)
        val btnReject: Button = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_job_offer, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val jobOffer = items[position]

        holder.name.text = jobOffer.landownerName
        holder.requestDate.text = "Work Date: ${jobOffer.workDate}"
        holder.address.text = "${jobOffer.village}, ${jobOffer.upazila}"

        // Show status and enable/disable buttons based on request status
        when(jobOffer.status) {
            "Pending" -> {
                holder.btnAccept.isEnabled = true
                holder.btnReject.isEnabled = true
                holder.btnAccept.text = "Accept"
                holder.btnReject.text = "Reject"
            }
            "Accepted" -> {
                holder.btnAccept.isEnabled = false
                holder.btnReject.isEnabled = false
                holder.btnAccept.text = "✓ Accepted"
                holder.btnReject.text = "Accepted"
            }
            "Rejected" -> {
                holder.btnAccept.isEnabled = false
                holder.btnReject.isEnabled = false
                holder.btnAccept.text = "❌ Rejected"
                holder.btnReject.text = "Rejected"
            }
        }

        // Row item click listener
        holder.itemView.setOnClickListener {
            onItemClick(jobOffer)
        }

        // Accept button click listener
        holder.btnAccept.setOnClickListener {
            onAcceptClick(jobOffer)
        }

        // Reject button click listener
        holder.btnReject.setOnClickListener {
            onRejectClick(jobOffer)
        }
    }
}