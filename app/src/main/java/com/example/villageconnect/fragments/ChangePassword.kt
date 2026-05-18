package com.example.villageconnect.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.villageconnect.R
import com.example.villageconnect.data.DBHelper
import com.example.villageconnect.data.DataAccess
import com.example.villageconnect.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ChangePassword : Fragment(R.layout.fragment_change_password) {

    private lateinit var edtCurrentPassword: TextInputEditText
    private lateinit var edtNewPassword: TextInputEditText
    private lateinit var edtConfirmPassword: TextInputEditText
    private lateinit var btnChangePassword: MaterialButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        edtCurrentPassword = view.findViewById(R.id.edtCurrentPassword)
        edtNewPassword = view.findViewById(R.id.edtNewPassword)
        edtConfirmPassword = view.findViewById(R.id.edtConfirmPassword)
        btnChangePassword = view.findViewById(R.id.btnChangePassword)

        btnChangePassword.setOnClickListener {
            updatePassword()
        }
    }

    private fun updatePassword() {
        val currentPass = edtCurrentPassword.text.toString().trim()
        val newPass = edtNewPassword.text.toString().trim()
        val confirmPass = edtConfirmPassword.text.toString().trim()

        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPass != confirmPass) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPass.length < 8) {
            Toast.makeText(requireContext(), "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = SessionManager(requireContext()).getUserId()

        // Verify current password
        val verifySql = "SELECT * FROM ${DBHelper.TABLE_USERS} WHERE ${DBHelper.COL_ID} = ? AND ${DBHelper.COL_PASSWORD} = ?"
        val cursor = DataAccess.executeQuery(requireContext(), verifySql, arrayOf(userId.toString(), currentPass))
        
        val isValid = cursor?.use { it.count > 0 } ?: false

        if (!isValid) {
            Toast.makeText(requireContext(), "Incorrect current password", Toast.LENGTH_SHORT).show()
            return
        }

        // Update password
        val updateSql = "UPDATE ${DBHelper.TABLE_USERS} SET ${DBHelper.COL_PASSWORD} = ? WHERE ${DBHelper.COL_ID} = ?"
        val result = DataAccess.executeDMLQuery(requireContext(), updateSql, arrayOf(newPass, userId))

        if (result) {
            Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_SHORT).show()
            
            // Clear inputs
            edtCurrentPassword.text?.clear()
            edtNewPassword.text?.clear()
            edtConfirmPassword.text?.clear()
            
            parentFragmentManager.popBackStack()
        } else {
            Toast.makeText(requireContext(), "Failed to update password", Toast.LENGTH_SHORT).show()
        }
    }
}