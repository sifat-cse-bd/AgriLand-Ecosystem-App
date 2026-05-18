package com.example.villageconnect.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DaySliderDialog(
    private val context: Context,
    private val days: List<LocalDate>,
    private val onDateSelected: (LocalDate) -> Unit
) {
    fun show() {
        val dateStrings = days.map { it.format(DateTimeFormatter.ofPattern("EEE, MMM dd (yyyy-MM-dd)")) }.toTypedArray()

        AlertDialog.Builder(context)
            .setTitle("Select a Work Date")
            .setItems(dateStrings) { _, which ->
                onDateSelected(days[which])
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }
}
