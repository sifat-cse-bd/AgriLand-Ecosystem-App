package com.example.villageconnect.landowner.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.villageconnect.R
import com.example.villageconnect.data.DBHelper
import com.example.villageconnect.data.DataAccess
import com.example.villageconnect.utils.SessionManager
import com.google.android.material.button.MaterialButton

class LandownerProfile : Fragment(R.layout.fragment_landowner_profile) {

    private lateinit var edtProfileName: EditText
    private lateinit var edtProfilePhone: EditText
    private lateinit var edtDistrict: EditText
    private lateinit var edtUpazila: EditText
    private lateinit var edtVillageName: EditText
    private lateinit var btnUpdateProfile: MaterialButton

    private var landownerId = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        edtProfileName = view.findViewById(R.id.edtProfileName)
        edtProfilePhone = view.findViewById(R.id.edtProfilePhone)
        edtDistrict = view.findViewById(R.id.edtDistrict)
        edtUpazila = view.findViewById(R.id.edtUpazila)
        edtVillageName = view.findViewById(R.id.edtVillageName)
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile)

        landownerId = SessionManager(requireContext()).getUserId()

        loadProfile()

        btnUpdateProfile.setOnClickListener {
            updateProfile()
        }
    }

    private fun loadProfile() {
        val sql = """
            SELECT * FROM ${DBHelper.TABLE_USERS}
            WHERE ${DBHelper.COL_ID} = ?
            LIMIT 1
        """

        val cursor = DataAccess.executeQuery(
            requireContext(),
            sql,
            arrayOf(landownerId.toString())
        )

        cursor?.use {
            if (it.moveToFirst()) {
                edtProfileName.setText(it.getString(it.getColumnIndexOrThrow(DBHelper.COL_FULL_NAME)))
                edtProfilePhone.setText(it.getString(it.getColumnIndexOrThrow(DBHelper.COL_PHONE)))
                edtDistrict.setText(it.getString(it.getColumnIndexOrThrow(DBHelper.COL_DISTRICT)) ?: "")
                edtUpazila.setText(it.getString(it.getColumnIndexOrThrow(DBHelper.COL_UPAZILA)) ?: "")
                edtVillageName.setText(it.getString(it.getColumnIndexOrThrow(DBHelper.COL_VILLAGE_NAME)) ?: "")
            }
        }
    }

    private fun updateProfile() {
        val name = edtProfileName.text.toString().trim()
        val district = edtDistrict.text.toString().trim()
        val upazila = edtUpazila.text.toString().trim()
        val village = edtVillageName.text.toString().trim()

        if (name.isEmpty() || district.isEmpty() || upazila.isEmpty() || village.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val sql = """
            UPDATE ${DBHelper.TABLE_USERS}
            SET ${DBHelper.COL_FULL_NAME} = ?,
                ${DBHelper.COL_DISTRICT} = ?,
                ${DBHelper.COL_UPAZILA} = ?,
                ${DBHelper.COL_VILLAGE_NAME} = ?
            WHERE ${DBHelper.COL_ID} = ?
        """

        val result = DataAccess.executeDMLQuery(
            requireContext(),
            sql,
            arrayOf(name, district, upazila, village, landownerId)
        )

        if (result) {
            Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
        }
    }
}