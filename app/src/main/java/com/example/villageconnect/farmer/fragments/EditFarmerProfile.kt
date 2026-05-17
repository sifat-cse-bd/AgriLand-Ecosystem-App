package com.example.villageconnect.farmer.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.villageconnect.R
import com.example.villageconnect.data.DBHelper
import com.example.villageconnect.data.DataAccess
import com.example.villageconnect.utils.SessionManager

class EditFarmerProfile : Fragment() {

    private lateinit var etFarmerName: EditText
    private lateinit var tvWarningName: TextView

    private lateinit var etFarmerPhone: EditText
    private lateinit var tvWarningPhone: TextView

    private lateinit var etFarmerDistrict: EditText
    private lateinit var tvWarningDistrict: TextView

    private lateinit var etFarmerUpazila: EditText
    private lateinit var tvWarningUpazila: TextView

    private lateinit var etFarmerVillage: EditText
    private lateinit var tvWarningVillage: TextView

    private lateinit var etFarmerSkills: EditText
    private lateinit var tvWarningSkills: TextView

    private lateinit var etFarmerExperience: EditText
    private lateinit var tvWarningExperience: TextView

    private lateinit var etFarmerWage: EditText
    private lateinit var tvWarningWage: TextView

    private lateinit var etFarmerBio: EditText
    private lateinit var tvWarningBio: TextView

    private lateinit var btnSaveFarmer: Button

    private var farmerId = -1
    private val db = DataAccess
    private var initialPhone = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_farmer_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        farmerId = SessionManager(requireContext()).getUserId()

        loadCurrentProfileData()
        setupLiveValidation()

        btnSaveFarmer.setOnClickListener {
            saveProfileData()
        }
    }

    private fun initViews(view: View) {
        etFarmerName = view.findViewById(R.id.etFarmerName)
        tvWarningName = view.findViewById(R.id.tvWarningName)

        etFarmerPhone = view.findViewById(R.id.etFarmerPhone)
        tvWarningPhone = view.findViewById(R.id.tvWarningPhone)

        etFarmerDistrict = view.findViewById(R.id.etFarmerDistrict)
        tvWarningDistrict = view.findViewById(R.id.tvWarningDistrict)

        etFarmerUpazila = view.findViewById(R.id.etFarmerUpazila)
        tvWarningUpazila = view.findViewById(R.id.tvWarningUpazila)

        etFarmerVillage = view.findViewById(R.id.etFarmerVillage)
        tvWarningVillage = view.findViewById(R.id.tvWarningVillage)

        etFarmerSkills = view.findViewById(R.id.etFarmerSkills)
        tvWarningSkills = view.findViewById(R.id.tvWarningSkills)

        etFarmerExperience = view.findViewById(R.id.etFarmerExperience)
        tvWarningExperience = view.findViewById(R.id.tvWarningExperience)

        etFarmerWage = view.findViewById(R.id.etFarmerWage)
        tvWarningWage = view.findViewById(R.id.tvWarningWage)

        etFarmerBio = view.findViewById(R.id.etFarmerBio)
        tvWarningBio = view.findViewById(R.id.tvWarningBio)

        btnSaveFarmer = view.findViewById(R.id.btnSaveFarmer)
    }

    private fun setupLiveValidation() {
        // Name Validation
        setUpValidation(etFarmerName, tvWarningName, { it.isNotEmpty() }, "Valid name", "Name cannot be empty")

        // Phone Live validation
        etFarmerPhone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val phone = s.toString().trim()
                if (phone.isEmpty()) {
                    showError(tvWarningPhone, "Phone number cannot be empty")
                    return
                }
                if (!phone.matches(Regex("^01[3-9][0-9]{8}$"))) {
                    showError(tvWarningPhone, "Invalid phone number")
                    return
                }
                if (phone != initialPhone && isPhoneExists(phone)) {
                    showError(tvWarningPhone, "Phone number already exists")
                } else {
                    showSuccess(tvWarningPhone, "Valid phone number")
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Common Text valid logic fields
        setUpValidation(etFarmerDistrict, tvWarningDistrict, { it.isNotEmpty() }, "Valid entry", "District is required")
        setUpValidation(etFarmerUpazila, tvWarningUpazila, { it.isNotEmpty() }, "Valid entry", "Upazila is required")
        setUpValidation(etFarmerVillage, tvWarningVillage, { it.isNotEmpty() }, "Valid entry", "Village is required")
        setUpValidation(etFarmerSkills, tvWarningSkills, { it.isNotEmpty() }, "Valid entry", "Skills are required")
        setUpValidation(etFarmerExperience, tvWarningExperience, { it.toIntOrNull() != null }, "Valid entry", "Enter a valid number of years")
        setUpValidation(etFarmerWage, tvWarningWage, { (it.toDoubleOrNull() ?: 0.0) > 0.0 }, "Valid entry", "Daily wage must be greater than 0")
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
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadCurrentProfileData() {
        val sql = """
            SELECT u.${DBHelper.COL_FULL_NAME}, u.${DBHelper.COL_PHONE}, u.${DBHelper.COL_DISTRICT}, 
                   u.${DBHelper.COL_UPAZILA}, u.${DBHelper.COL_VILLAGE_NAME}, f.${DBHelper.COL_SKILLS}, 
                   f.${DBHelper.COL_EXPERIENCE}, f.${DBHelper.COL_DAILY_WAGE}, f.${DBHelper.COL_BIO}
            FROM ${DBHelper.TABLE_USERS} u
            LEFT JOIN ${DBHelper.TABLE_FARMER_PROFILES} f ON u.id = f.${DBHelper.COL_USER_ID}
            WHERE u.id = ?
        """.trimIndent()

        val cursor = db.executeQuery(requireContext(), sql, arrayOf(farmerId.toString()))
        cursor?.use {
            if (it.moveToFirst()) {
                initialPhone = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_PHONE)) ?: ""

                etFarmerName.setText(it.getString(it.getColumnIndexOrThrow(DBHelper.COL_FULL_NAME)) ?: "")
                etFarmerPhone.setText(initialPhone)
                etFarmerDistrict.setText(it.getString(it.getColumnIndexOrThrow(DBHelper.COL_DISTRICT)) ?: "")
                etFarmerUpazila.setText(it.getString(it.getColumnIndexOrThrow(DBHelper.COL_UPAZILA)) ?: "")
                etFarmerVillage.setText(it.getString(it.getColumnIndexOrThrow(DBHelper.COL_VILLAGE_NAME)) ?: "")
                etFarmerSkills.setText(it.getString(it.getColumnIndexOrThrow(DBHelper.COL_SKILLS)) ?: "")
                etFarmerExperience.setText(it.getString(it.getColumnIndexOrThrow(DBHelper.COL_EXPERIENCE)) ?: "")

                val wage = it.getDouble(it.getColumnIndexOrThrow(DBHelper.COL_DAILY_WAGE))
                etFarmerWage.setText(if(wage > 0) wage.toString() else "")
                etFarmerBio.setText(it.getString(it.getColumnIndexOrThrow(DBHelper.COL_BIO)) ?: "")
            }
        }
    }

    private fun isPhoneExists(phone: String): Boolean {
        val sql = "SELECT ${DBHelper.COL_ID} FROM ${DBHelper.TABLE_USERS} WHERE ${DBHelper.COL_PHONE} = ? LIMIT 1"
        val cursor = db.executeQuery(requireContext(), sql, arrayOf(phone))
        var exists = false
        try {
            if (cursor != null && cursor.moveToFirst()) exists = true
        } finally {
            cursor?.close()
        }
        return exists
    }

    private fun validateForm(): Boolean {
        var isValid = true
        if (etFarmerName.text.toString().trim().isEmpty()) { showError(tvWarningName, "Name cannot be empty"); isValid = false }

        val phone = etFarmerPhone.text.toString().trim()
        if (!phone.matches(Regex("^01[3-9][0-9]{8}$"))) { showError(tvWarningPhone, "Invalid phone number"); isValid = false }
        if (phone != initialPhone && isPhoneExists(phone)) { showError(tvWarningPhone, "Phone already exists"); isValid = false }

        if (etFarmerDistrict.text.toString().trim().isEmpty()) { showError(tvWarningDistrict, "District required"); isValid = false }
        if (etFarmerUpazila.text.toString().trim().isEmpty()) { showError(tvWarningUpazila, "Upazila required"); isValid = false }
        if (etFarmerVillage.text.toString().trim().isEmpty()) { showError(tvWarningVillage, "Village required"); isValid = false }
        if (etFarmerSkills.text.toString().trim().isEmpty()) { showError(tvWarningSkills, "Skills required"); isValid = false }
        if (etFarmerExperience.text.toString().trim().toIntOrNull() == null) { showError(tvWarningExperience, "Invalid experience"); isValid = false }
        if ((etFarmerWage.text.toString().trim().toDoubleOrNull() ?: 0.0) <= 0.0) { showError(tvWarningWage, "Invalid daily wage"); isValid = false }

        return isValid
    }

    private fun saveProfileData() {
        if (!validateForm()) return

        val name = etFarmerName.text.toString().trim()
        val phone = etFarmerPhone.text.toString().trim()
        val district = etFarmerDistrict.text.toString().trim()
        val upazila = etFarmerUpazila.text.toString().trim()
        val village = etFarmerVillage.text.toString().trim()
        val skills = etFarmerSkills.text.toString().trim()
        val experience = etFarmerExperience.text.toString().trim()
        val wage = etFarmerWage.text.toString().trim().toDouble()
        val bio = etFarmerBio.text.toString().trim()

        val userUpdateSql = """
            UPDATE ${DBHelper.TABLE_USERS} 
            SET ${DBHelper.COL_FULL_NAME} = ?, ${DBHelper.COL_PHONE} = ?, ${DBHelper.COL_DISTRICT} = ?, ${DBHelper.COL_UPAZILA} = ?, ${DBHelper.COL_VILLAGE_NAME} = ? 
            WHERE id = ?
        """.trimIndent()

        val userUpdated = db.executeDMLQuery(requireContext(), userUpdateSql, arrayOf(name, phone, district, upazila, village, farmerId))

        val checkProfileSql = "SELECT COUNT(*) FROM ${DBHelper.TABLE_FARMER_PROFILES} WHERE ${DBHelper.COL_USER_ID} = ?"
        val hasProfile = db.executeScalarInt(requireContext(), checkProfileSql, arrayOf(farmerId.toString())) > 0

        val profileSuccess: Boolean
        if (hasProfile) {
            val profileUpdateSql = """
                UPDATE ${DBHelper.TABLE_FARMER_PROFILES} 
                SET ${DBHelper.COL_SKILLS} = ?, ${DBHelper.COL_EXPERIENCE} = ?, ${DBHelper.COL_DAILY_WAGE} = ?, ${DBHelper.COL_BIO} = ? 
                WHERE ${DBHelper.COL_USER_ID} = ?
            """.trimIndent()
            profileSuccess = db.executeDMLQuery(requireContext(), profileUpdateSql, arrayOf(skills, experience, wage, bio, farmerId))
        } else {
            val profileInsertSql = """
                INSERT INTO ${DBHelper.TABLE_FARMER_PROFILES} (${DBHelper.COL_USER_ID}, ${DBHelper.COL_SKILLS}, ${DBHelper.COL_EXPERIENCE}, ${DBHelper.COL_DAILY_WAGE}, ${DBHelper.COL_BIO}) 
                VALUES (?, ?, ?, ?, ?)
            """.trimIndent()
            profileSuccess = db.executeDMLQuery(requireContext(), profileInsertSql, arrayOf(farmerId, skills, experience, wage, bio))
        }

        if (userUpdated && profileSuccess) {
            Toast.makeText(requireContext(), "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        } else {
            Toast.makeText(requireContext(), "Failed to update profile!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showError(textView: TextView, message: String) {
        textView.visibility = View.VISIBLE
        textView.text = message
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorError))
    }

    private fun showSuccess(textView: TextView, message: String) {
        textView.visibility = View.VISIBLE
        textView.text = message
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorSecondary))
    }
}