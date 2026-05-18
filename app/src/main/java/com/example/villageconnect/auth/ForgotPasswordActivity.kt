package com.example.villageconnect.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.villageconnect.R
import com.example.villageconnect.data.DBHelper
import com.example.villageconnect.data.DataAccess

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var dotStep1: View
    private lateinit var dotStep2: View
    private lateinit var dotStep3: View

    private lateinit var step1Layout: View
    private lateinit var step2Layout: View
    private lateinit var step3Layout: View

    // Step 1 Views
    private lateinit var edtForgotPhone: EditText
    private lateinit var btnVerifyPhone: Button

    // Step 2 Views
    private lateinit var edtOtpCode: EditText
    private lateinit var btnVerifyOtp: Button
    private lateinit var tvOtpSentTo: TextView
    private lateinit var tvResendOtp: TextView

    // Step 3 Views
    private lateinit var edtNewPassword: EditText
    private lateinit var edtConfirmPassword: EditText
    private lateinit var btnResetPassword: Button


    private var generatedOtp = "123456" // default otp. Replace with actual OTP generation logic
    private var userPhone = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        dotStep1 = findViewById(R.id.dotStep1)
        dotStep2 = findViewById(R.id.dotStep2)
        dotStep3 = findViewById(R.id.dotStep3)

        step1Layout = findViewById(R.id.step1Layout)
        step2Layout = findViewById(R.id.step2Layout)
        step3Layout = findViewById(R.id.step3Layout)

        // Step 1
        edtForgotPhone = findViewById(R.id.edtForgotPhone)
        btnVerifyPhone = findViewById(R.id.btnVerifyPhone)

        // Step 2
        edtOtpCode = findViewById(R.id.edtOtpCode)
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp)
        tvOtpSentTo = findViewById(R.id.tvOtpSentTo)
        tvResendOtp = findViewById(R.id.tvResendOtp)

        // Step 3
        edtNewPassword = findViewById(R.id.edtNewPassword)
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword)
        btnResetPassword = findViewById(R.id.btnResetPassword)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Step 1: Search Phone
        btnVerifyPhone.setOnClickListener {
            val phone = edtForgotPhone.text.toString().trim()
            if (isValidPhone(phone)) {
                checkUserExists(phone)
            } else {
                Toast.makeText(this, getString(R.string.enter_valid_phone), Toast.LENGTH_SHORT).show()
            }
        }

        // Step 2: Verify OTP
        btnVerifyOtp.setOnClickListener {
            val enteredOtp = edtOtpCode.text.toString().trim()
            if (enteredOtp == generatedOtp) {
                showStep(3)
            } else {
                Toast.makeText(this, getString(R.string.invalid_otp_hint), Toast.LENGTH_SHORT).show()
            }
        }

        tvResendOtp.setOnClickListener {
            sendOtp(userPhone)
        }

        // Step 3: Reset Password
        btnResetPassword.setOnClickListener {
            val newPass = edtNewPassword.text.toString()
            val confirmPass = edtConfirmPassword.text.toString()

            if (newPass.length < 8) {
                Toast.makeText(this, getString(R.string.ErrorPassword), Toast.LENGTH_SHORT).show()
            } else if (newPass != confirmPass) {
                Toast.makeText(this, getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show()
            } else {
                updatePassword(newPass)
            }
        }
    }

    private fun checkUserExists(phone: String) {
        val query = "SELECT ${DBHelper.COL_PHONE} FROM ${DBHelper.TABLE_USERS} WHERE ${DBHelper.COL_PHONE} = ?"
        val cursor = DataAccess.executeQuery(this, query, arrayOf(phone))

        if (cursor != null && cursor.moveToFirst()) {
            userPhone = phone
            cursor.close()
            sendOtp(phone)
            showStep(2)
        } else {
            Toast.makeText(this, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendOtp(phone: String) {
        // Simulating OTP sending
        generatedOtp = (100000..999999).random().toString()
        tvOtpSentTo.text = getString(R.string.otp_sent_to, phone)
        Toast.makeText(this, "OTP Sent (Simulated): $generatedOtp", Toast.LENGTH_LONG).show()
    }

    private fun updatePassword(newPass: String) {
        val sql = "UPDATE ${DBHelper.TABLE_USERS} SET ${DBHelper.COL_PASSWORD} = ? WHERE ${DBHelper.COL_PHONE} = ?"
        val success = DataAccess.executeDMLQuery(this, sql, arrayOf(newPass, userPhone))

        if (success) {
            Toast.makeText(this, getString(R.string.password_reset_success), Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, getString(R.string.failed_to_update_password), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showStep(step: Int) {
        step1Layout.visibility = View.GONE
        step2Layout.visibility = View.GONE
        step3Layout.visibility = View.GONE

        dotStep1.setBackgroundResource(R.drawable.circle_indicator_inactive)
        dotStep2.setBackgroundResource(R.drawable.circle_indicator_inactive)
        dotStep3.setBackgroundResource(R.drawable.circle_indicator_inactive)

        when (step) {
            1 -> {
                step1Layout.visibility = View.VISIBLE
                dotStep1.setBackgroundResource(R.drawable.circle_indicator_active)
            }
            2 -> {
                step2Layout.visibility = View.VISIBLE
                dotStep2.setBackgroundResource(R.drawable.circle_indicator_active)
            }
            3 -> {
                step3Layout.visibility = View.VISIBLE
                dotStep3.setBackgroundResource(R.drawable.circle_indicator_active)
            }
        }
    }

    private fun isValidPhone(phone: String): Boolean {
        return phone.matches(Regex("^01[3-9][0-9]{8}$"))
    }
}
