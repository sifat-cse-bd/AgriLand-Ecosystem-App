package com.example.villageconnect.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.villageconnect.R
import com.google.android.material.card.MaterialCardView
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class DaySliderDialog(
    private val context: Context,
    private val availableDates: List<LocalDate>,
    private val bookedDates: List<LocalDate> = emptyList(),
    private val onDateSelected: (LocalDate) -> Unit
) {
    fun show() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_day_selection, null)
        val rvDateGrid = view.findViewById<RecyclerView>(R.id.rvDateGrid)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        // Combine and sort all dates for display
        val allDisplayDates = (availableDates + bookedDates).distinct().sorted()

        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .create()

        rvDateGrid.layoutManager = GridLayoutManager(context, 3)
        rvDateGrid.adapter = DateAdapter(allDisplayDates, bookedDates) { selectedDate ->
            onDateSelected(selectedDate)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private inner class DateAdapter(
        private val dates: List<LocalDate>,
        private val booked: List<LocalDate>,
        private val onClick: (LocalDate) -> Unit
    ) : RecyclerView.Adapter<DateAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val card: MaterialCardView = view.findViewById(R.id.cardDate)
            val tvDay: TextView = view.findViewById(R.id.tvDayName)
            val tvDate: TextView = view.findViewById(R.id.tvDateNumber)
            val tvMonth: TextView = view.findViewById(R.id.tvMonthName)
            val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_date_selection, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val date = dates[position]
            val isBooked = booked.contains(date)

            holder.tvDay.text = date.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH))
            holder.tvDate.text = date.dayOfMonth.toString()
            holder.tvMonth.text = date.format(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)).uppercase()

            if (isBooked) {
                holder.tvStatus.text = "BOOKED"
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.error))
                holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.surfaceVariant))
                holder.card.strokeColor = ContextCompat.getColor(context, R.color.outline)
                holder.card.isEnabled = false
                holder.card.alpha = 0.6f
            } else {
                holder.tvStatus.text = "AVAILABLE"
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.success))
                holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface))
                holder.card.strokeColor = ContextCompat.getColor(context, R.color.primary)
                holder.card.isEnabled = true
                holder.card.alpha = 1.0f
                holder.card.setOnClickListener { onClick(date) }
            }
        }

        override fun getItemCount() = dates.size
    }
}
