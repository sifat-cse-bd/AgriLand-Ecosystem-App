package com.example.villageconnect.landowner.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.villageconnect.R
import com.example.villageconnect.data.DBHelper
import com.example.villageconnect.data.DataAccess
import com.example.villageconnect.landowner.LandownerMainActivity
import com.example.villageconnect.landowner.adapters.FarmerAdapter
import com.example.villageconnect.landowner.models.FarmerItem
import com.example.villageconnect.utils.SessionManager
import com.example.villageconnect.utils.DaySliderDialog
import com.google.android.material.chip.Chip
import java.time.LocalDate
import java.time.LocalTime

class FarmerList : Fragment(R.layout.fragment_farmer_list) {

    lateinit var chipHireFarmer: Chip
    lateinit var chipActive: Chip
    lateinit var chipCompleted: Chip
    lateinit var chipCancelled: Chip
    lateinit var chipHistory: Chip

    private lateinit var rvFarmers: RecyclerView
    private var landownerId = -1
    private var upazillaName: String? = null
    private val farmers = mutableListOf<FarmerItem>()

    private var currentFilter = "hire"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvFarmers = view.findViewById(R.id.rvFarmers)
        rvFarmers.layoutManager = LinearLayoutManager(requireContext())

        landownerId = SessionManager(requireContext()).getUserId()
        upazillaName = getLandownerUpazilla()

        chipHireFarmer = view.findViewById(R.id.chipHireFarmer)
        chipActive = view.findViewById(R.id.chipActive)
        chipCompleted = view.findViewById(R.id.chipCompleted)
        chipCancelled = view.findViewById(R.id.chipCancelled)
        chipHistory = view.findViewById(R.id.chipHistory)

        chipHireFarmer.setOnClickListener { currentFilter = "hire"; applyFilter() }
        chipActive.setOnClickListener { currentFilter = "active"; applyFilter() }
        chipCompleted.setOnClickListener { currentFilter = "completed"; applyFilter() }
        chipCancelled.setOnClickListener { currentFilter = "cancelled"; applyFilter() }
        chipHistory.setOnClickListener { currentFilter = "history"; applyFilter() }

        loadFarmers()
    }

    private fun getLandownerUpazilla(): String? {
        val sql = "SELECT ${DBHelper.COL_UPAZILA} FROM ${DBHelper.TABLE_USERS} WHERE ${DBHelper.COL_ID} = ?"
        val cursor = DataAccess.executeQuery(requireContext(), sql, arrayOf(landownerId.toString()))
        var upazilla: String? = null
        cursor?.use {
            if(it.moveToFirst()) upazilla = it.getString(0)
        }
        return upazilla
    }

    private fun loadFarmers() {
        val sql = """
            SELECT u.${DBHelper.COL_ID}, u.${DBHelper.COL_FULL_NAME}, u.${DBHelper.COL_PHONE}, u.${DBHelper.COL_VILLAGE_NAME},
                   fp.${DBHelper.COL_SKILLS}, fp.${DBHelper.COL_EXPERIENCE}, fp.${DBHelper.COL_DAILY_WAGE}
            FROM ${DBHelper.TABLE_USERS} u
            LEFT JOIN ${DBHelper.TABLE_FARMER_PROFILES} fp ON u.${DBHelper.COL_ID} = fp.${DBHelper.COL_USER_ID}
            WHERE u.${DBHelper.COL_ROLE} = ?
              AND u.${DBHelper.COL_UPAZILA} = ?
        """
        try {
            val cursor = DataAccess.executeQuery(requireContext(), sql, arrayOf(DBHelper.ROLE_FARMER, upazillaName ?: ""))
            cursor?.use {
                while(it.moveToNext()){
                    farmers.add(FarmerItem(
                        id = it.getInt(0),
                        name = it.getString(1),
                        phone = it.getString(2),
                        village = it.getString(3),
                        skills = it.getString(4) ?: "No skills added",
                        experience = it.getString(5) ?: "No experience",
                        dailyWage = if(!it.isNull(6)) it.getDouble(6) else 0.0,
                        hasPendingRequest = checkPendingRequest(it.getInt(0))
                    ))
                }
            }
            applyFilter()
        } catch(e: Exception){
            Log.e("LoadFarmers_Error", "Error loading farmers: ${e.message}", e)
        }
    }

    private fun checkPendingRequest(farmerId: Int): Boolean {
        val sql = """
            SELECT COUNT(*) FROM ${DBHelper.TABLE_HIRE_REQUESTS} 
            WHERE ${DBHelper.COL_LANDOWNER_ID} = ? AND ${DBHelper.COL_FARMER_ID} = ? AND ${DBHelper.COL_REQUEST_STATUS} = '${DBHelper.STATUS_PENDING}'
        """
        return DataAccess.executeScalarInt(requireContext(), sql, arrayOf(landownerId.toString(), farmerId.toString())) > 0
    }

    private fun cancelHireRequest(farmer: FarmerItem) {
        val sql = """
            DELETE FROM ${DBHelper.TABLE_HIRE_REQUESTS} 
            WHERE ${DBHelper.COL_LANDOWNER_ID} = ? AND ${DBHelper.COL_FARMER_ID} = ? AND ${DBHelper.COL_REQUEST_STATUS} = '${DBHelper.STATUS_PENDING}'
        """
        DataAccess.executeDMLQuery(requireContext(), sql, arrayOf<Any>(landownerId, farmer.id))
        farmer.hasPendingRequest = false
        farmer.selectedDate = null
        applyFilter()
    }

    private fun selectDayAndSendRequest(farmer: FarmerItem) {
        val now = LocalTime.now()
        val today = LocalDate.now()
        val next10Days = mutableListOf<LocalDate>()
        if(now.isBefore(LocalTime.of(7,0))) next10Days.add(today)
        for(i in 1..10) next10Days.add(today.plusDays(i.toLong()))

        val dayDialog = DaySliderDialog(requireContext(), next10Days) { selectedDate ->
            sendHireRequest(farmer, selectedDate)
            farmer.hasPendingRequest = true
            farmer.selectedDate = selectedDate.toString()
            applyFilter()
        }
        dayDialog.show()
    }

    private fun sendHireRequest(farmer: FarmerItem, date: LocalDate) {
        val sql = """
            INSERT INTO ${DBHelper.TABLE_HIRE_REQUESTS} 
            (${DBHelper.COL_LANDOWNER_ID}, ${DBHelper.COL_FARMER_ID}, ${DBHelper.COL_WORK_DATE}, ${DBHelper.COL_REQUEST_STATUS}, ${DBHelper.COL_WORK_STATUS})
            VALUES (?, ?, ?, ?, ?)
        """
        DataAccess.executeDMLQuery(requireContext(), sql, arrayOf<Any>(landownerId, farmer.id, date.toString(), DBHelper.STATUS_PENDING, DBHelper.STATUS_PENDING))
    }

    // ---------------- Filter Logic ----------------
    private fun applyFilter(){
        val filtered = when(currentFilter){
            "hire" -> farmers.filter { !it.hasPendingRequest }
            "active" -> farmers.filter { it.hasPendingRequest }
            "completed" -> farmers.filter { it.workStatus == DBHelper.STATUS_COMPLETED }
            "cancelled" -> farmers.filter { it.requestStatus == DBHelper.STATUS_REJECTED }
            "history" -> farmers.filter { !it.selectedDate.isNullOrEmpty() && LocalDate.parse(it.selectedDate).isBefore(LocalDate.now()) }
            else -> farmers
        }
        rvFarmers.adapter = FarmerAdapter(
            items = filtered,
            onCardClick = { farmer -> /* navigate to details if needed */ },
            onButtonClick = { farmer ->
                if (farmer.hasPendingRequest) {
                    cancelHireRequest(farmer)
                } else {
                    selectDayAndSendRequest(farmer)
                }
            },
            filterType = currentFilter
        )
    }
}
