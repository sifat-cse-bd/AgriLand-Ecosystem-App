package com.example.villageconnect.landowner.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.villageconnect.R
import com.example.villageconnect.data.DBHelper
import com.example.villageconnect.data.DataAccess
import com.example.villageconnect.utils.SessionManager
import com.google.android.material.button.MaterialButton
import java.util.Calendar

class FarmerDetailsFragment : Fragment(R.layout.fragment_farmer_details) {

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
        fun newInstance(farmerId: Int): FarmerDetailsFragment {
            val fragment = FarmerDetailsFragment()
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

        loadFarmerDetails()
        setupDatePicker()

        btnHireFarmer.setOnClickListener {
            sendHireRequest()
        }
    }

    private fun loadFarmerDetails() {
        val sql = """
            SELECT u.${DBHelper.COL_FULL_NAME},
                   u.${DBHelper.COL_VILLAGE_NAME},
                   fp.${DBHelper.COL_SKILLS},
                   fp.${DBHelper.COL_EXPERIENCE},
                   fp.${DBHelper.COL_DAILY_WAGE}
            FROM ${DBHelper.TABLE_USERS} u
            LEFT JOIN ${DBHelper.TABLE_FARMER_PROFILES} fp
            ON u.${DBHelper.COL_ID} = fp.${DBHelper.COL_USER_ID}
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
            val calendar = Calendar.getInstance()

            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    edtWorkDate.setText("$day/${month + 1}/$year")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun sendHireRequest() {
        val workDate = edtWorkDate.text.toString().trim()

        if (workDate.isEmpty()) {
            Toast.makeText(requireContext(), "Select work date", Toast.LENGTH_SHORT).show()
            return
        }

        val sql = """
            INSERT INTO ${DBHelper.TABLE_HIRE_REQUESTS}
            (${DBHelper.COL_LANDOWNER_ID}, ${DBHelper.COL_FARMER_ID}, ${DBHelper.COL_WORK_DATE}, ${DBHelper.COL_STATUS})
            VALUES (?, ?, ?, ?)
        """

        val result = DataAccess.executeDMLQuery(
            requireContext(),
            sql,
            arrayOf(landownerId, farmerId, workDate, DBHelper.STATUS_PENDING)
        )

        if (result) {
            Toast.makeText(requireContext(), "Hire request sent", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Request failed", Toast.LENGTH_SHORT).show()
        }
    }
}