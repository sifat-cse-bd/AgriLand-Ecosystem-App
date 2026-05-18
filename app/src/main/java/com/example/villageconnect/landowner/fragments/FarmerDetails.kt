package com.example.villageconnect.landowner.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.villageconnect.R
import com.example.villageconnect.data.DBHelper
import com.example.villageconnect.data.DataAccess
import com.example.villageconnect.utils.SessionManager
import com.example.villageconnect.utils.DaySliderDialog
import com.google.android.material.button.MaterialButton
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class FarmerDetails : Fragment(R.layout.fragment_farmer_details) {

    private lateinit var tvName: TextView
    private lateinit var tvVillage: TextView
    private lateinit var tvSkills: TextView
    private lateinit var tvExperience: TextView
    private lateinit var tvWage: TextView
    private lateinit var edtWorkDate: EditText
    private lateinit var btnHireFarmer: MaterialButton

    private var farmerId = -1
    private var landownerId = -1

    companion object {
        fun newInstance(farmerId: Int): FarmerDetails {
            val fragment = FarmerDetails()
            val bundle = Bundle()
            bundle.putInt("farmer_id", farmerId)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tvName = view.findViewById(R.id.tvFarmerDetailsName)
        tvVillage = view.findViewById(R.id.tvFarmerDetailsVillage)
        tvSkills = view.findViewById(R.id.tvFarmerDetailsSkills)
        tvExperience = view.findViewById(R.id.tvFarmerDetailsExperience)
        tvWage = view.findViewById(R.id.tvFarmerDetailsWage)
        edtWorkDate = view.findViewById(R.id.edtWorkDate)
        btnHireFarmer = view.findViewById(R.id.btnHireFarmer)

        farmerId = arguments?.getInt("farmer_id") ?: -1
        landownerId = SessionManager(requireContext()).getUserId()

        view.findViewById<View>(R.id.btnBackFarmerDetails).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        loadFarmerDetails()
        setupDatePicker()

        btnHireFarmer.setOnClickListener {
            sendHireRequest()
        }
    }

    private fun loadFarmerDetails() {
        val sql = """
            SELECT u.${DBHelper.COL_FULL_NAME}, u.${DBHelper.COL_VILLAGE_NAME},
                   fp.${DBHelper.COL_SKILLS}, fp.${DBHelper.COL_EXPERIENCE}, fp.${DBHelper.COL_DAILY_WAGE}
            FROM ${DBHelper.TABLE_USERS} u
            LEFT JOIN ${DBHelper.TABLE_FARMER_PROFILES} fp ON u.${DBHelper.COL_ID} = fp.${DBHelper.COL_USER_ID}
            WHERE u.${DBHelper.COL_ID} = ?
        """
        val cursor = DataAccess.executeQuery(requireContext(), sql, arrayOf(farmerId.toString()))
        cursor?.use {
            if (it.moveToFirst()) {
                tvName.text = it.getString(0)
                tvVillage.text = it.getString(1) ?: "Village not updated"
                tvSkills.text = "Skills: ${it.getString(2) ?: "Not added"}"
                tvExperience.text = "Experience: ${it.getString(3) ?: "Not added"}"
                tvWage.text = "Daily Wage: ৳${if (!it.isNull(4)) it.getDouble(4) else 0.0}"
            }
        }
    }

    private fun setupDatePicker() {
        edtWorkDate.setOnClickListener {
            val bookedDates = getFarmerBookedDates(farmerId)
            val selectableDates = mutableListOf<LocalDate>()
            val today = LocalDate.now()
            
            // Available dates logic (next 10 days)
            if(LocalTime.now().isBefore(LocalTime.of(7,0))) selectableDates.add(today)
            for(i in 1..10) selectableDates.add(today.plusDays(i.toLong()))
            
            val filteredSelectable = selectableDates.filter { !bookedDates.contains(it) }

            val dialog = DaySliderDialog(requireContext(), filteredSelectable.toMutableList(), bookedDates.toList()) { selectedDate ->
                edtWorkDate.setText(selectedDate.toString())
            }
            dialog.show()
        }
    }

    private fun getFarmerBookedDates(farmerId: Int): Set<LocalDate> {
        val bookedDates = mutableSetOf<LocalDate>()
        val sql = "SELECT ${DBHelper.COL_WORK_DATE} FROM ${DBHelper.TABLE_HIRE_REQUESTS} WHERE ${DBHelper.COL_FARMER_ID} = ? AND ${DBHelper.COL_REQUEST_STATUS} = '${DBHelper.STATUS_ACCEPTED}'"
        val cursor = DataAccess.executeQuery(requireContext(), sql, arrayOf(farmerId.toString()))
        cursor?.use {
            while (it.moveToNext()) {
                try { bookedDates.add(LocalDate.parse(it.getString(0))) } catch (e: Exception) {}
            }
        }
        return bookedDates
    }

    private fun sendHireRequest() {
        val workDateStr = edtWorkDate.text.toString().trim()
        if (workDateStr.isEmpty()) {
            Toast.makeText(requireContext(), "Select work date", Toast.LENGTH_SHORT).show()
            return
        }

        val workDate = LocalDate.parse(workDateStr)
        val expiryTime = LocalDateTime.of(workDate.minusDays(if(workDate == LocalDate.now()) 0 else 1), LocalTime.of(7, 0))

        val sql = """
            INSERT INTO ${DBHelper.TABLE_HIRE_REQUESTS} 
            (${DBHelper.COL_LANDOWNER_ID}, ${DBHelper.COL_FARMER_ID}, ${DBHelper.COL_WORK_DATE}, ${DBHelper.COL_REQUEST_STATUS}, ${DBHelper.COL_EXPIRES_AT})
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(landowner_id, farmer_id, work_date) DO UPDATE SET
                ${DBHelper.COL_REQUEST_STATUS} = EXCLUDED.${DBHelper.COL_REQUEST_STATUS},
                ${DBHelper.COL_EXPIRES_AT} = EXCLUDED.${DBHelper.COL_EXPIRES_AT},
                ${DBHelper.COL_UPDATED_AT} = CURRENT_TIMESTAMP
        """
        val result = DataAccess.executeDMLQuery(requireContext(), sql, arrayOf<Any>(landownerId, farmerId, workDate.toString(), DBHelper.STATUS_PENDING, expiryTime.toString()))

        if (result) {
            Toast.makeText(requireContext(), "Hire request sent", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        } else {
            Toast.makeText(requireContext(), "Request failed", Toast.LENGTH_SHORT).show()
        }
    }
}
