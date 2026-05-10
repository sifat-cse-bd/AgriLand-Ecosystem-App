package com.example.villageconnect

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import java.util.Calendar

private var TextView.setTextcolor: Int
    get() = throw UnsupportedOperationException("Property does not have a getter")
    set(value) {
        this.setTextColor(value)
    }

class Registration : AppCompatActivity() {
    lateinit var edtName: EditText
    lateinit var edtPhone: EditText
    lateinit var tvErrorPhone: TextView
    lateinit var edtNID: EditText
    lateinit var tvErrorNID: TextView
    lateinit var dtpDOB: EditText
    lateinit var ivDtp: ImageView
    lateinit var tvErrorDOB: TextView
    lateinit var cmbDistrict: AutoCompleteTextView
    lateinit var cmbRole: AutoCompleteTextView
    lateinit var edtPassword: EditText
    lateinit var tvErrorPassword: TextView


    lateinit var btnRegister: MaterialButton


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        edtName = findViewById<EditText>(R.id.edtName)

        edtPhone = findViewById<EditText>(R.id.edtPhone)
        tvErrorPhone = findViewById<TextView>(R.id.tvErrorPhone)

        edtNID = findViewById<EditText>(R.id.edtNID)
        tvErrorNID = findViewById<TextView>(R.id.tvErrorNID)

        dtpDOB = findViewById<EditText>(R.id.dtpDOB)
        ivDtp = findViewById<ImageView>(R.id.ivDtp)
        tvErrorDOB = findViewById<TextView>(R.id.tvErrorDOB)

        cmbDistrict = findViewById<AutoCompleteTextView>(R.id.cmbDistrict)
        cmbRole = findViewById<AutoCompleteTextView>(R.id.cmbRole)

        edtPassword = findViewById<EditText>(R.id.edtPassword)
        tvErrorPassword = findViewById<TextView>(R.id.tvErrorPassword)

        btnRegister = findViewById<MaterialButton>(R.id.btnRegister)

        //validation text field
        setUpValidation(edtPhone, tvErrorPhone, { it.matches(Regex("^01[3-9][0-9]{8}$")) }, "Valid phone number", "Invalid phone number")
        setUpValidation(edtNID,tvErrorNID, { it.matches(Regex("^[0-9]{10}$")) }, "Valid NID", "Invalid NID")
        setUpValidation(edtPassword, tvErrorPassword, { it.length >= 8 }, "Valid password", "Invalid password")

        //date time picker
        showDatePicker()

        //populate dropdown
        setUpDropDown(cmbDistrict, DropDownData.districts.map { it.name })
        setUpDropDown(cmbRole, DropDownData.roles.map { it.name })


        //action of registration button
        btnRegister.setOnClickListener{

        }


    }

    fun setUpDropDown(view: AutoCompleteTextView, items: List<String>) {

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
    fun showDatePicker() {
        val listener = View.OnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker =  DatePickerDialog(this,
                {_, selectedYear, selectedMonth, selectedDay ->
                    val dob = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    dtpDOB.setText(dob)
                }, year, month, day)

            datePicker.datePicker.maxDate = System.currentTimeMillis()
            datePicker.show()
        }
        dtpDOB.setOnClickListener(listener)
        ivDtp.setOnClickListener(listener)
    }

    fun setUpValidation(
        editText: EditText,
        errorTextView:TextView,
        validator: (String)->Boolean,
        validMsg:String,
        invalidMsg:String)
    {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?)
            {
                val text = s.toString()
                if(text.isEmpty()){
                    errorTextView.visibility = View.GONE
                    return
                }
                val isValid = validator(text)
                errorTextView.visibility = View.VISIBLE
                errorTextView.text = if(isValid) validMsg else invalidMsg
                errorTextView.setTextcolor = if(isValid) Color.GREEN else Color.RED
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }


}


