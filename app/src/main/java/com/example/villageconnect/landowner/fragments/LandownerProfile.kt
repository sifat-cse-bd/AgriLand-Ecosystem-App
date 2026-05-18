package com.example.villageconnect.landowner.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.villageconnect.R
import com.example.villageconnect.data.DBHelper
import com.example.villageconnect.data.DataAccess
import com.example.villageconnect.models.DropDownData
import com.example.villageconnect.fragments.ChangePassword
import com.example.villageconnect.utils.SessionManager
import com.google.android.material.button.MaterialButton

class LandownerProfile : Fragment(R.layout.fragment_landowner_profile) {

    private lateinit var edtProfileName: EditText
    private lateinit var spinnerDistrict: Spinner
    private lateinit var spinnerUpazila: Spinner
    private lateinit var edtVillageName: EditText
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnUpdateProfile: MaterialButton
    private lateinit var btnChangePassword: MaterialButton
    private lateinit var btnLogout: MaterialButton
    private lateinit var btnDeleteAccount: MaterialButton
    private var tvProfileName: TextView? = null

    private var landownerId = -1
    private var isEditMode = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        edtProfileName = view.findViewById(R.id.tvProfileName)
        spinnerDistrict = view.findViewById(R.id.spinnerDistrict)
        spinnerUpazila = view.findViewById(R.id.spinnerUpazila)
        edtVillageName = view.findViewById(R.id.edtVillageName)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile)
        btnChangePassword = view.findViewById(R.id.btnChangePassword)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount)

        landownerId = SessionManager(requireContext()).getUserId()

        loadProfile()
        setupDistrictSpinner()

        btnEditProfile.setOnClickListener { toggleEditMode(true) }
        btnUpdateProfile.setOnClickListener { updateProfile() }
        btnDeleteAccount.setOnClickListener { deleteAccount() }
        btnLogout.setOnClickListener { logout() }
        btnChangePassword.setOnClickListener { 
            parentFragmentManager.beginTransaction()
                .replace(R.id.landownerFragmentContainer, ChangePassword())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadProfile() {
        val sql = "SELECT * FROM ${DBHelper.TABLE_USERS} WHERE ${DBHelper.COL_ID} = ? LIMIT 1"
        val cursor = DataAccess.executeQuery(requireContext(), sql, arrayOf(landownerId.toString()))
        cursor?.use {
            if (it.moveToFirst()) {
                edtProfileName.setText(it.getString(it.getColumnIndexOrThrow(DBHelper.COL_FULL_NAME)))
                edtVillageName.setText(it.getString(it.getColumnIndexOrThrow(DBHelper.COL_VILLAGE_NAME)) ?: "")
                val district = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_DISTRICT)) ?: ""
                val upazila = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_UPAZILA)) ?: ""
                spinnerDistrict.setSelection(DropDownData.districts.indexOfFirst { d -> d.name == district }.coerceAtLeast(0))
                loadUpazilaSpinner(district)
                spinnerUpazila.setSelection(DropDownData.upazilas.entries.firstOrNull { it.value.contains(upazila) }?.value?.indexOf(upazila) ?: 0)
            }
        }
        toggleEditMode(false)
    }

    private fun setupDistrictSpinner() {
        val districtNames = DropDownData.districts.map { it.name }
        spinnerDistrict.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, districtNames)
        spinnerDistrict.isEnabled = false

        spinnerDistrict.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedDistrict = districtNames[position]
                loadUpazilaSpinner(selectedDistrict)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinnerUpazila.isEnabled = false
    }

    private fun loadUpazilaSpinner(district: String) {
        val districtItem = DropDownData.districts.firstOrNull { it.name == district }
        val upazilaNames = districtItem?.id?.let { DropDownData.upazilas[it] } ?: emptyList()
        spinnerUpazila.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, upazilaNames)
    }

    private fun toggleEditMode(enable: Boolean) {
        isEditMode = enable
        edtProfileName.isEnabled = enable
        spinnerDistrict.isEnabled = enable
        spinnerUpazila.isEnabled = enable
        edtVillageName.isEnabled = enable
        btnUpdateProfile.visibility = if (enable) View.VISIBLE else View.GONE
    }

    private fun updateProfile() {
        if (!isEditMode) return

        val name = edtProfileName.text.toString().trim()
        val district = spinnerDistrict.selectedItem.toString()
        val upazila = spinnerUpazila.selectedItem.toString()
        val village = edtVillageName.text.toString().trim()

        if (name.isEmpty() || district.isEmpty() || upazila.isEmpty() || village.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val sql = """
            UPDATE ${DBHelper.TABLE_USERS}
            SET ${DBHelper.COL_FULL_NAME} = ?, ${DBHelper.COL_DISTRICT} = ?, ${DBHelper.COL_UPAZILA} = ?, ${DBHelper.COL_VILLAGE_NAME} = ?
            WHERE ${DBHelper.COL_ID} = ?
        """
        val result = DataAccess.executeDMLQuery(requireContext(), sql, arrayOf(name, district, upazila, village, landownerId))

        if (result) {
            Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
            toggleEditMode(false)
        } else {
            Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteAccount() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Yes") { _, _ ->
                val sql = "DELETE FROM ${DBHelper.TABLE_USERS} WHERE ${DBHelper.COL_ID} = ?"
                val result = DataAccess.executeDMLQuery(requireContext(), sql, arrayOf(landownerId))
                if (result) {
                    Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show()
                    logout()
                } else {
                    Toast.makeText(requireContext(), "Failed to delete account", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun logout() {
        SessionManager(requireContext()).logout()
        val intent = android.content.Intent(requireContext(), com.example.villageconnect.auth.Login::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }
}