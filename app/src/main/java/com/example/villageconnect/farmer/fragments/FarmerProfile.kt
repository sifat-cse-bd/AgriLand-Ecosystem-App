package com.example.villageconnect.farmer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.villageconnect.R
import com.example.villageconnect.auth.Login
import com.example.villageconnect.data.DBHelper
import com.example.villageconnect.data.DataAccess
import com.example.villageconnect.farmer.models.FarmerProfileItem
import com.example.villageconnect.utils.SessionManager
import com.google.android.material.button.MaterialButton
import androidx.fragment.app.FragmentManager
import com.example.villageconnect.farmer.fragments.EditFarmerProfile

class FarmerProfile : Fragment() {

    // Views Declaration
    private lateinit var tvProfileInitial: TextView
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileAddress: TextView
    private lateinit var tvProfilePhone: TextView
    private lateinit var tvDailyWage: TextView
    private lateinit var tvSkills: TextView
    private lateinit var tvExperience: TextView
    private lateinit var tvBio: TextView
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnLogout: MaterialButton

    private var farmerId: Int = -1
    private var farmerProfileItem: FarmerProfileItem? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_farmer_profile, container, false)

        initViews(view)

        farmerId = SessionManager(requireContext()).getUserId()

        loadFarmerInfo()
        displayFarmerInfo()

        setupClickListeners()

        return view
    }

    private fun initViews(view: View) {
        tvProfileInitial = view.findViewById(R.id.tvProfileInitial)
        tvProfileName = view.findViewById(R.id.tvProfileName)
        tvProfileAddress = view.findViewById(R.id.tvProfileAddress)
        tvProfilePhone = view.findViewById(R.id.tvProfilePhone)
        tvDailyWage = view.findViewById(R.id.tvDailyWage)
        tvSkills = view.findViewById(R.id.tvSkills)
        tvExperience = view.findViewById(R.id.tvExperience)
        tvBio = view.findViewById(R.id.tvBio)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnLogout = view.findViewById(R.id.btnLogout)
    }

    private fun loadFarmerInfo() {
        val sql = """
            SELECT 
                u.id AS user_id,
                u.${DBHelper.COL_FULL_NAME},
                u.${DBHelper.COL_PHONE},
                u.${DBHelper.COL_DISTRICT},
                u.${DBHelper.COL_UPAZILA},
                u.${DBHelper.COL_VILLAGE_NAME},
                f.${DBHelper.COL_SKILLS},
                f.${DBHelper.COL_EXPERIENCE},
                f.${DBHelper.COL_DAILY_WAGE},
                f.${DBHelper.COL_BIO}
            FROM ${DBHelper.TABLE_USERS} u
            LEFT JOIN ${DBHelper.TABLE_FARMER_PROFILES} f ON u.id = f.${DBHelper.COL_USER_ID}
            WHERE u.id = ?
        """.trimIndent()

        try {
            val cursor = DataAccess.executeQuery(requireContext(), sql, arrayOf(farmerId.toString()))
            cursor?.use {
                if (it.moveToFirst()) {
                    farmerProfileItem = FarmerProfileItem(
                        userId = it.getInt(it.getColumnIndexOrThrow("user_id")),
                        fullName = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_FULL_NAME))
                            ?: "",
                        phone = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_PHONE)) ?: "",
                        village = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_VILLAGE_NAME))
                            ?: "",
                        upazila = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_UPAZILA))
                            ?: "",
                        district = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_DISTRICT))
                            ?: "",
                        skills = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_SKILLS)) ?: "",
                        experience = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_EXPERIENCE))
                            ?: "",
                        dailyWage = it.getDouble(it.getColumnIndexOrThrow(DBHelper.COL_DAILY_WAGE)),
                        bio = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_BIO)) ?: ""
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("FarmerDashboard_Error", "LEFT JOIN query failed: ${e.message}", e)
        }
    }

    private fun displayFarmerInfo() {
        farmerProfileItem?.let { item ->

            if (item.fullName.isNotEmpty()) {
                tvProfileInitial.text = item.fullName.trim().first().uppercase()
            }

            // Set Header Info
            tvProfileName.text = item.fullName
            tvProfilePhone.text = item.phone

            // Format Address smoothly (Village, Upazila, District)
            val fullAddress = listOf(item.village, item.upazila, item.district)
                .filter { it.isNotEmpty() }
                .joinToString(", ")
            tvProfileAddress.text = if (fullAddress.isNotEmpty()) fullAddress else "Address not set"

            // Set Professional Details
            tvDailyWage.text = "৳ ${item.dailyWage.toInt()} / day"

            tvSkills.text = if (item.skills.isNotEmpty()) "${item.skills} years" else "No skills added yet"
            tvExperience.text = if (item.experience.isNotEmpty()) item.experience else "No experience details added"
            tvBio.text = if (item.bio.isNotEmpty()) item.bio else "No bio written yet"
        } ?: run {
            Toast.makeText(requireContext(), "Failed to load profile details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        btnEditProfile.setOnClickListener {
            loadFragment(EditFarmerProfile())
        }

        btnLogout.setOnClickListener {
            SessionManager(requireContext()).logout()

            val intent = Intent(requireContext(), Login::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }
    }
    fun loadFragment(fragment: Fragment)
    {
        parentFragmentManager.beginTransaction()
            .replace(R.id.farmerFragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

}