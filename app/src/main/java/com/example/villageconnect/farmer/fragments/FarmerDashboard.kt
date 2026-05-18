package com.example.villageconnect.farmer.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.villageconnect.R
import com.example.villageconnect.data.DBHelper
import com.example.villageconnect.data.DataAccess
import com.example.villageconnect.farmer.models.FarmerProfileItem
import com.example.villageconnect.utils.SessionManager
import com.example.villageconnect.utils.WorkStatusManager

class FarmerDashboard : Fragment() {
    private lateinit var tvFarmerWelcome: TextView
    private lateinit var tvAvailabilityStatus: TextView
    private lateinit var btnChangeAvailability: Button
    private lateinit var tvPendingJobsCount: TextView
    private lateinit var tvAcceptedJobsCount: TextView
    private lateinit var tvFarmerSkills: TextView
    private lateinit var tvFarmerExperience: TextView
    private lateinit var tvWageAmount: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var btnViewJobOffers: Button
    private lateinit var btnViewMyJobs: Button
    private lateinit var popupWarning: View

    private var farmerId = -1
    private val db = DataAccess

    private var farmerProfileItem = FarmerProfileItem(-1, "", "", "", "", "", "", "", 0.0, "")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_farmer_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        tvFarmerWelcome = view.findViewById(R.id.tvFarmerWelcome)
        tvAvailabilityStatus = view.findViewById(R.id.tvAvailabilityStatus)
        btnChangeAvailability = view.findViewById(R.id.btnChangeAvailability)
        tvPendingJobsCount = view.findViewById(R.id.tvPendingJobsCount)
        tvAcceptedJobsCount = view.findViewById(R.id.tvAcceptedJobsCount)
        tvFarmerSkills = view.findViewById(R.id.tvFarmerSkills)
        tvFarmerExperience = view.findViewById(R.id.tvFarmerExperience)
        tvWageAmount = view.findViewById(R.id.tvWageAmount)
        popupWarning = view.findViewById(R.id.editProfilePopUp)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnViewJobOffers = view.findViewById(R.id.btnViewJobOffers)
        btnViewMyJobs = view.findViewById(R.id.btnViewMyJobs)

        farmerId = SessionManager(requireContext()).getUserId()

        // Sync statuses (expiry/incomplete) before loading dashboard info
        WorkStatusManager.checkAndUpdateStatuses(requireContext())

        setUpDashboardInfo()


        btnEditProfile.setOnClickListener { loadFragment(EditFarmerProfile()) }
        btnViewJobOffers.setOnClickListener { loadFragment(JobOffers()) }
        btnViewMyJobs.setOnClickListener { loadFragment(MyJobs()) }

        popupWarning.setOnClickListener {
            popupWarning.visibility = View.GONE
            loadFragment(EditFarmerProfile())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.farmerFragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
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
            val cursor = db.executeQuery(requireContext(), sql, arrayOf(farmerId.toString()))
            cursor?.use {
                if (it.moveToFirst()) {
                    farmerProfileItem = FarmerProfileItem(
                        userId = it.getInt(it.getColumnIndexOrThrow("user_id")),
                        fullName = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_FULL_NAME)) ?: "",
                        phone = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_PHONE)) ?: "",
                        village = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_VILLAGE_NAME)) ?: "",
                        upazila = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_UPAZILA)) ?: "",
                        district = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_DISTRICT)) ?: "",
                        skills = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_SKILLS)) ?: "",
                        experience = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_EXPERIENCE)) ?: "",
                        dailyWage = it.getDouble(it.getColumnIndexOrThrow(DBHelper.COL_DAILY_WAGE)),
                        bio = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_BIO)) ?: ""
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("FarmerDashboard_Error", "LEFT JOIN query failed: ${e.message}", e)
        }
    }

    private fun checkStatus(): String {
        val sql = """
            SELECT ${DBHelper.COL_REQUEST_STATUS} 
            FROM ${DBHelper.TABLE_HIRE_REQUESTS} 
            WHERE ${DBHelper.COL_FARMER_ID} = ? 
            ORDER BY id DESC LIMIT 1
        """.trimIndent()

        var currentStatus = ""
        try {
            val cursor = db.executeQuery(requireContext(), sql, arrayOf(farmerId.toString()))
            cursor?.use {
                if (it.moveToFirst()) {
                    currentStatus = it.getString(0) ?: ""
                }
            }
        } catch (e: Exception) {
            Log.e("FarmerDashboard", "Status extraction error: ${e.message}")
        }
        return currentStatus
    }

    private fun statusChangeDashboard() {
        val currentStatus = checkStatus()
        if (currentStatus == DBHelper.STATUS_ACCEPTED) {
            tvAvailabilityStatus.text = DBHelper.STATUS_ON_WORK
        } else {
            tvAvailabilityStatus.text = DBHelper.STATUS_AVAILABLE
        }
    }

    private fun pendingOffersCount(): String {
        // Only count pending requests that haven't expired
        val sql = """
            SELECT COUNT(*) FROM ${DBHelper.TABLE_HIRE_REQUESTS} 
            WHERE ${DBHelper.COL_FARMER_ID} = ? 
              AND ${DBHelper.COL_REQUEST_STATUS} = '${DBHelper.STATUS_PENDING}'
              AND (expires_at > CURRENT_TIMESTAMP OR expires_at IS NULL)
        """.trimIndent()
        return db.executeScalarInt(requireContext(), sql, arrayOf(farmerId.toString())).toString()
    }

    private fun activeWorkCount(): String {
        val sql = """
            SELECT COUNT(*) FROM ${DBHelper.TABLE_HIRE_WORK} hw
            JOIN ${DBHelper.TABLE_HIRE_REQUESTS} hr ON hw.${DBHelper.COL_HIRE_REQUEST_ID} = hr.${DBHelper.COL_ID}
            WHERE hr.${DBHelper.COL_FARMER_ID} = ? 
              AND hw.${DBHelper.COL_WORK_STATUS} = '${DBHelper.STATUS_ON_WORK}'
        """.trimIndent()
        return db.executeScalarInt(requireContext(), sql, arrayOf(farmerId.toString())).toString()
    }

    private fun completedJobsCount(): String {
        val sql = """
            SELECT COUNT(*) FROM ${DBHelper.TABLE_HIRE_WORK} hw
            JOIN ${DBHelper.TABLE_HIRE_REQUESTS} hr ON hw.${DBHelper.COL_HIRE_REQUEST_ID} = hr.${DBHelper.COL_ID}
            WHERE hr.${DBHelper.COL_FARMER_ID} = ? 
              AND hw.${DBHelper.COL_WORK_STATUS} = '${DBHelper.STATUS_COMPLETED}'
        """.trimIndent()
        return db.executeScalarInt(requireContext(), sql, arrayOf(farmerId.toString())).toString()
    }

    private fun checkAddress() {
        val p = farmerProfileItem
        if (p.village.isBlank() || p.upazila.isBlank() || p.district.isBlank() ||
            p.village == "not found" || p.upazila == "not found" || p.district == "not found") {
            popupWarning.visibility = View.VISIBLE
        } else {
            popupWarning.visibility = View.GONE
        }
    }

    private fun setUpDashboardInfo() {
        loadFarmerInfo() 
        tvFarmerWelcome.text = "Welcome, ${farmerProfileItem.fullName}"
        statusChangeDashboard()
        checkAddress()
        
        tvPendingJobsCount.text = pendingOffersCount()
        // We'll show Completed count in the second box as before, but updated logic
        tvAcceptedJobsCount.text = completedJobsCount()
        
        tvFarmerSkills.text = "Skills: ${farmerProfileItem.skills}"
        tvFarmerExperience.text = "Experience: ${farmerProfileItem.experience}"
        tvWageAmount.text = "${farmerProfileItem.dailyWage.toInt()} BDT"
    }
}