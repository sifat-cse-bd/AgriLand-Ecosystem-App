package com.example.villageconnect.farmer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.villageconnect.R
import com.example.villageconnect.farmer.models.MyJobItem
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class MyJobAdapter(
    private val items: List<MyJobItem>,
    private val onStartWorkClick: (MyJobItem) -> Unit,
    private val onCompleteClick: (MyJobItem) -> Unit,
    private val onCancelClick: (MyJobItem) -> Unit
) : RecyclerView.Adapter<MyJobAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val landownerName: TextView = view.findViewById(R.id.tvLandownerName)
        val workDate: TextView = view.findViewById(R.id.tvRequestDate)
        val address: TextView = view.findViewById(R.id.tvAddress)
        val status: TextView = view.findViewById(R.id.tvSelectedDate)
        val tvHireIds: TextView = view.findViewById(R.id.tvHireIds)
        val btnPrimary: Button = view.findViewById(R.id.btnRequest)
        val btnSecondary: Button? = view.findViewById<Button?>(R.id.btnSecondary)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_job, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val job = items[position]
        val today = LocalDate.now()
        val workDateObj = LocalDate.parse(job.workDate)
        val daysUntilWork = ChronoUnit.DAYS.between(today, workDateObj)

        holder.landownerName.text = job.landownerName
        holder.workDate.text = "Work Date: ${job.workDate}"
        holder.address.text = "${job.village} | Check in: ${job.phone}"

        val reqIdStr = "R-${job.hireRequestId}"
        val workIdStr = if (job.hireWorkId != null) "W-${job.hireWorkId}" else "N/A"
        holder.tvHireIds.text = "Req: $reqIdStr | Work: $workIdStr"

        // Dynamic status display
        val statusText = when(job.workStatus) {
            "On-Work" -> "🔴 ON WORK - Started"
            "Completed" -> "✅ COMPLETED"
            "Cancelled" -> "❌ CANCELLED"
            else -> "⏳ PENDING"
        }
        holder.status.text = statusText

        // Dynamic buttons based on work status
        when(job.workStatus) {
            "Pending" -> {
                // Show "Start Work" button
                holder.btnPrimary.visibility = View.VISIBLE
                holder.btnPrimary.text = "Start Work"
                holder.btnPrimary.setOnClickListener { onStartWorkClick(job) }

                // Show "Cancel" if 2+ days notice
                if(holder.btnSecondary != null) {
                    holder.btnSecondary.visibility = if(daysUntilWork >= 2) View.VISIBLE else View.GONE
                    holder.btnSecondary.text = "Cancel"
                    holder.btnSecondary.setOnClickListener { onCancelClick(job) }
                }
            }
            "On-Work" -> {
                // Show "Complete Work" button
                holder.btnPrimary.visibility = View.VISIBLE
                holder.btnPrimary.text = "Complete Work"
                holder.btnPrimary.setOnClickListener { onCompleteClick(job) }

                // Hide secondary button
                if(holder.btnSecondary != null) holder.btnSecondary.visibility = View.GONE
            }
            else -> {
                // Hide button for completed/cancelled
                holder.btnPrimary.visibility = View.GONE
                if(holder.btnSecondary != null) holder.btnSecondary.visibility = View.GONE
            }
        }
    }
}