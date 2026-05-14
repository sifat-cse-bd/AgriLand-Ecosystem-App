package com.example.villageconnect.auth

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
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
import com.example.villageconnect.models.DropDownData
import com.google.android.material.button.MaterialButton
import java.util.Calendar

class Registration : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtPhone: EditText
    private lateinit var tvErrorPhone: TextView
    private lateinit var edtNID: EditText
    private lateinit var tvErrorNID: TextView
    private lateinit var dtpDOB: EditText
    private lateinit var ivDtp: ImageView
    private lateinit var tvErrorDOB: TextView
    private lateinit var cmbRole: AutoCompleteTextView
    private lateinit var edtPassword: EditText
    private lateinit var tvErrorPassword: TextView
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvSignIn: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupLiveValidation()
        showDatePicker()
        setupDropDown(cmbRole, DropDownData.roles.map { it.name })

        btnRegister.setOnClickListener {
            registerUser()
        }

        tvSignIn.setOnClickListener {
            // Login activity thakle ekhane open korba
            // startActivity(Intent(this, Login::class.java))
            finish()
        }
    }

    private fun initViews() {
        edtName = findViewById(R.id.edtName)

        edtPhone = findViewById(R.id.edtPhone)
        tvErrorPhone = findViewById(R.id.tvErrorPhone)

        edtNID = findViewById(R.id.edtNID)
        tvErrorNID = findViewById(R.id.tvErrorNID)

        dtpDOB = findViewById(R.id.dtpDOB)
        ivDtp = findViewById(R.id.ivDtp)
        tvErrorDOB = findViewById(R.id.tvErrorDOB)

        cmbRole = findViewById(R.id.cmbRole)

        edtPassword = findViewById(R.id.edtPassword)
        tvErrorPassword = findViewById(R.id.tvErrorPassword)

        btnRegister = findViewById(R.id.btnRegister)
        tvSignIn = findViewById(R.id.tvSignIn)
    }

    private fun setupLiveValidation() {
        setUpPhoneLiveValidation()
        setUpNidLiveValidation()

        setUpValidation(
            editText = edtPassword,
            errorTextView = tvErrorPassword,
            validator = { it.length >= 8 },
            validMsg = "Valid password",
            invalidMsg = "Password must be at least 8 characters"
        )
    }

    private fun registerUser() {
        val name = edtName.text.toString().trim()
        val phone = edtPhone.text.toString().trim()
        val nid = edtNID.text.toString().trim()
        val dob = dtpDOB.text.toString().trim()
        val role = cmbRole.text.toString().trim()
        val password = edtPassword.text.toString().trim()

        if (!validateRegistrationForm(name, phone, nid, dob, role, password)) {
            return
        }

        // Final phone duplicate check
        if (isPhoneExists(phone)) {
            showError(
                tvErrorPhone,
                "Phone number already exists"
            )
            edtPhone.requestFocus()
            return
        }

        // Final NID duplicate check
        if (isNidExists(nid)) {
            showError(
                tvErrorNID,
                "NID already exists"
            )
            edtNID.requestFocus()
            return
        }

        val sql = """
            INSERT INTO ${DBHelper.TABLE_USERS}
            (
                ${DBHelper.COL_FULL_NAME},
                ${DBHelper.COL_NID},
                ${DBHelper.COL_PHONE},
                ${DBHelper.COL_DOB},
                ${DBHelper.COL_PASSWORD},
                ${DBHelper.COL_ROLE}
            )
            VALUES (?, ?, ?, ?, ?, ?)
        """

        val result = DataAccess.executeDMLQuery(
            this,
            sql,
            arrayOf(
                name,
                nid,
                phone,
                dob,
                password,
                convertRoleForDatabase(role)
            )
        )

        if (result) {
            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
            clearForm()

            // Login page e pathate chaile uncomment korba
            // startActivity(Intent(this, Login::class.java))
            // finish()

        } else {
            Toast.makeText(
                this,
                "Registration failed. Please try again.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun validateRegistrationForm(
        name: String,
        phone: String,
        nid: String,
        dob: String,
        role: String,
        password: String
    ): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter full name", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (!phone.matches(Regex("^01[3-9][0-9]{8}$"))) {
            showError(tvErrorPhone, "Invalid phone number")
            isValid = false
        }

        if (!nid.matches(Regex("^[0-9]{10}$|^[0-9]{17}$"))) {
            showError(tvErrorNID, "Invalid NID")
            isValid = false
        }

        if (dob.isEmpty()) {
            showError(tvErrorDOB, "Please select date of birth")
            isValid = false
        }

        if (role.isEmpty()) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (password.length < 8) {
            showError(tvErrorPassword, "Password must be at least 8 characters")
            isValid = false
        }

        return isValid
    }

    private fun isPhoneExists(phone: String): Boolean {
        val sql = """
            SELECT ${DBHelper.COL_ID}
            FROM ${DBHelper.TABLE_USERS}
            WHERE ${DBHelper.COL_PHONE} = ?
            LIMIT 1
        """

        val cursor = DataAccess.executeQuery(
            this,
            sql,
            arrayOf(phone)
        )

        var exists = false

        try {
            if (cursor != null && cursor.moveToFirst()) {
                exists = true
            }
        } finally {
            cursor?.close()
        }

        return exists
    }

    private fun isNidExists(nid: String): Boolean {
        val sql = """
            SELECT ${DBHelper.COL_ID}
            FROM ${DBHelper.TABLE_USERS}
            WHERE ${DBHelper.COL_NID} = ?
            LIMIT 1
        """

        val cursor = DataAccess.executeQuery(
            this,
            sql,
            arrayOf(nid)
        )

        var exists = false

        try {
            if (cursor != null && cursor.moveToFirst()) {
                exists = true
            }
        } finally {
            cursor?.close()
        }

        return exists
    }

    private fun setUpPhoneLiveValidation() {
        edtPhone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val phone = s.toString().trim()

                if (phone.isEmpty()) {
                    tvErrorPhone.visibility = View.GONE
                    return
                }

                if (!phone.matches(Regex("^01[3-9][0-9]{8}$"))) {
                    showError(tvErrorPhone, "Invalid phone number")
                    return
                }

                if (isPhoneExists(phone)) {
                    showError(tvErrorPhone, "Phone number already exists")
                } else {
                    showSuccess(tvErrorPhone, "Valid phone number")
                }
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }
        })
    }

    private fun setUpNidLiveValidation() {
        edtNID.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val nid = s.toString().trim()

                if (nid.isEmpty()) {
                    tvErrorNID.visibility = View.GONE
                    return
                }

                if (!nid.matches(Regex("^[0-9]{10}$|^[0-9]{17}$"))) {
                    showError(tvErrorNID, "Invalid NID")
                    return
                }

                if (isNidExists(nid)) {
                    showError(tvErrorNID, "NID already exists")
                } else {
                    showSuccess(tvErrorNID, "Valid NID")
                }
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }
        })
    }

    private fun setUpValidation(
        editText: EditText,
        errorTextView: TextView,
        validator: (String) -> Boolean,
        validMsg: String,
        invalidMsg: String
    ) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString().trim()

                if (text.isEmpty()) {
                    errorTextView.visibility = View.GONE
                    return
                }

                if (validator(text)) {
                    showSuccess(errorTextView, validMsg)
                } else {
                    showError(errorTextView, invalidMsg)
                }
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }
        })
    }

    private fun showDatePicker() {
        val listener = View.OnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val dob = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    dtpDOB.setText(dob)
                    tvErrorDOB.visibility = View.GONE
                },
                year,
                month,
                day
            )

            datePicker.datePicker.maxDate = System.currentTimeMillis()
            datePicker.show()
        }

        dtpDOB.setOnClickListener(listener)
        ivDtp.setOnClickListener(listener)
    }

    private fun setupDropDown(view: AutoCompleteTextView, items: List<String>) {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            items
        )

        view.setAdapter(adapter)

        view.dropDownVerticalOffset =
            (5 * resources.displayMetrics.density).toInt()

        view.setDropDownBackgroundDrawable(
            ContextCompat.getDrawable(this, R.drawable.village_bg_gradient)
        )

        val itemHeightDp = 48
        val maxVisibleItems = 4
        val maxHeightPx =
            (itemHeightDp * maxVisibleItems * resources.displayMetrics.density).toInt()

        view.dropDownHeight = maxHeightPx

        view.setOnClickListener {
            view.showDropDown()
        }
    }

    private fun convertRoleForDatabase(role: String): String {
        return when (role.lowercase()) {
            "landowner" -> DBHelper.ROLE_LANDOWNER
            "farmer" -> DBHelper.ROLE_FARMER
            "merchant" -> DBHelper.ROLE_MERCHANT
            "agri-merchant" -> DBHelper.ROLE_MERCHANT
            "agri merchant" -> DBHelper.ROLE_MERCHANT
            else -> role.lowercase()
        }
    }

    private fun showError(textView: TextView, message: String) {
        textView.visibility = View.VISIBLE
        textView.text = message
        textView.setTextColor(ContextCompat.getColor(this, R.color.colorError))
    }

    private fun showSuccess(textView: TextView, message: String) {
        textView.visibility = View.VISIBLE
        textView.text = message
        textView.setTextColor(ContextCompat.getColor(this, R.color.colorSecondary))
    }

    private fun clearForm() {
        edtName.text.clear()
        edtPhone.text.clear()
        edtNID.text.clear()
        dtpDOB.text.clear()
        cmbRole.text.clear()
        edtPassword.text.clear()

        tvErrorPhone.visibility = View.GONE
        tvErrorNID.visibility = View.GONE
        tvErrorDOB.visibility = View.GONE
        tvErrorPassword.visibility = View.GONE
    }
}