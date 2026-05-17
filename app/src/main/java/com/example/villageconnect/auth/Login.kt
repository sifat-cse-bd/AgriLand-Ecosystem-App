package com.example.villageconnect.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
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
import com.example.villageconnect.farmer.FarmerMainActivity
//import com.example.villageconnect.farmer.FarmerDashboardActivity
import com.example.villageconnect.landowner.LandownerMainActivity
//import com.example.villageconnect.merchant.MerchantDashboardActivity
import com.example.villageconnect.utils.SessionManager
import com.google.android.material.button.MaterialButton

class Login : AppCompatActivity() {

    private lateinit var edtLoginPhone: EditText
    private lateinit var edtLoginPassword: EditText
    private lateinit var tvErrorPhone: TextView
    private lateinit var tvErrorPassword: TextView
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvSignUp: TextView
    private lateinit var btnLogin: MaterialButton
    private lateinit var progressOverlay: View
    private lateinit var dot1: View
    private lateinit var dot2: View
    private lateinit var dot3: View


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupLiveValidation()

        btnLogin.setOnClickListener {
            loginUser()
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, Registration::class.java))
        }

        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot password feature will be added later", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        edtLoginPhone = findViewById(R.id.edtLoginPhone)
        edtLoginPassword = findViewById(R.id.edtLoginPassword)

        tvErrorPhone = findViewById(R.id.tvErrorPhone)
        tvErrorPassword = findViewById(R.id.tvErrorPassword)

        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        tvSignUp = findViewById(R.id.tvSignUp)

        btnLogin = findViewById(R.id.btnLogin)

        progressOverlay = findViewById(R.id.progressOverlay)
        dot1 = findViewById(R.id.dot1)
        dot2 = findViewById(R.id.dot2)
        dot3 = findViewById(R.id.dot3)

    }

    private fun setupLiveValidation() {
        edtLoginPhone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val phone = s.toString().trim()

                if (phone.isEmpty()) {
                    tvErrorPhone.visibility = View.GONE
                    return
                }

                if (!isValidPhone(phone)) {
                    showError(tvErrorPhone, "Invalid phone number")
                } else {
                    tvErrorPhone.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        edtLoginPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()

                if (password.isEmpty()) {
                    tvErrorPassword.visibility = View.GONE
                    return
                }

                if (password.length < 8) {
                    showError(tvErrorPassword, "Password must be at least 8 characters")
                } else {
                    tvErrorPassword.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loginUser() {
        val phone = edtLoginPhone.text.toString().trim()
        val password = edtLoginPassword.text.toString().trim()

        if (!validateLoginForm(phone, password)) {
            return
        }

        val sql = """
            SELECT ${DBHelper.COL_ID}, ${DBHelper.COL_ROLE}, ${DBHelper.COL_FULL_NAME}
            FROM ${DBHelper.TABLE_USERS}
            WHERE ${DBHelper.COL_PHONE} = ?
            AND ${DBHelper.COL_PASSWORD} = ?
            LIMIT 1
        """

        val cursor = DataAccess.executeQuery(
            this,
            sql,
            arrayOf(phone, password)
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val userId = it.getInt(it.getColumnIndexOrThrow(DBHelper.COL_ID))
                val role = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_ROLE))

                SessionManager(this).saveUserSession(userId, role)

                showLoadingWithDelay(2500) {
                    openDashboardByRole(role)
                }
            } else {
                Toast.makeText(this, getString(R.string.login_invalid_message), Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun startBubbleAnimation() {
        fun animateDot(view: View, delay: Long) {
            val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.5f, 1.0f)
            val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.5f, 1.0f)

            scaleX.repeatCount = ValueAnimator.INFINITE
            scaleY.repeatCount = ValueAnimator.INFINITE

            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 600
                startDelay = delay
                start()
            }
        }

        animateDot(dot1, 0)
        animateDot(dot2, 200)
        animateDot(dot3, 400)
    }

    private fun showLoadingWithDelay(delayMillis: Long, onDelayComplete: () -> Unit) {
        progressOverlay.visibility = View.VISIBLE
        startBubbleAnimation() // call animation

        Handler(Looper.getMainLooper()).postDelayed({
            progressOverlay.visibility = View.GONE
            onDelayComplete()
        }, delayMillis)
    }
    private fun validateLoginForm(phone: String, password: String): Boolean {
        var isValid = true

        if (!isValidPhone(phone)) {
            showError(tvErrorPhone, "Invalid phone number")
            edtLoginPhone.requestFocus()
            isValid = false
        }

        if (password.isEmpty()) {
            showError(tvErrorPassword, "Password is required")
            edtLoginPassword.requestFocus()
            isValid = false
        } else if (password.length < 8) {
            showError(tvErrorPassword, "Password must be at least 8 characters")
            edtLoginPassword.requestFocus()
            isValid = false
        }

        return isValid
    }

    private fun openDashboardByRole(role: String) {
        when (role.lowercase()) {
            DBHelper.ROLE_LANDOWNER -> {
                startActivity(Intent(this, LandownerMainActivity::class.java))
                finish()
            }

            DBHelper.ROLE_FARMER -> {
                startActivity(Intent(this, FarmerMainActivity::class.java))
                finish()
            }

            DBHelper.ROLE_MERCHANT -> {
//                startActivity(Intent(this, MerchantDashboardActivity::class.java))
                finish()
            }

            else -> {
                Toast.makeText(this, "Unknown user role", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isValidPhone(phone: String): Boolean {
        return phone.matches(Regex("^01[3-9][0-9]{8}$"))
    }

    private fun showError(textView: TextView, message: String) {
        textView.visibility = View.VISIBLE
        textView.text = message
        textView.setTextColor(ContextCompat.getColor(this, R.color.colorError))
    }
}