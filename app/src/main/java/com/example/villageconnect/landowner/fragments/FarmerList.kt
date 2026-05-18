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
import com.example.villageconnect.landowner.adapters.FarmerAdapter
import com.example.villageconnect.landowner.models.FarmerItem
import com.example.villageconnect.utils.SessionManager
import com.example.villageconnect.utils.DaySliderDialog
import com.google.android.material.chip.Chip
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class FarmerList : Fragment(R.layout.fragment_farmer_list) {

    lateinit var chipHireFarmer: Chip
    lateinit var chipActive: Chip
    lateinit var chipCompleted: Chip
    lateinit var chipCancelled: Chip
    lateinit var chipHistory: Chip
    lateinit var edtSearchFarmer: com.google.android.material.textfield.TextInputEditText

    private lateinit var rvFarmers: RecyclerView
    private var landownerId = -1
    private var upazillaName: String? = null
    private val allFarmers = mutableListOf<FarmerItem>()

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
        edtSearchFarmer = view.findViewById(R.id.edtSearchFarmer)

        chipHireFarmer.isChecked = true
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
        cursor?.use { if(it.moveToFirst()) upazilla = it.getString(0) }
        return upazilla
    }

    override fun onResume() {
        super.onResume()
        loadFarmers()
    }

    private fun loadFarmers() {
        allFarmers.clear()
        val sql = """
            SELECT u.${DBHelper.COL_ID}, u.${DBHelper.COL_FULL_NAME}, u.${DBHelper.COL_PHONE}, u.${DBHelper.COL_VILLAGE_NAME},
                   fp.${DBHelper.COL_SKILLS}, fp.${DBHelper.COL_EXPERIENCE}, fp.${DBHelper.COL_DAILY_WAGE},
                   hr.${DBHelper.COL_REQUEST_STATUS}, hr.${DBHelper.COL_WORK_DATE}, 
                   COALESCE(hw.${DBHelper.COL_WORK_STATUS}, '${DBHelper.STATUS_PENDING}'),
                   hr.${DBHelper.COL_ID} AS hireRequestId,
                   hw.${DBHelper.COL_ID} AS hireWorkId
            FROM ${DBHelper.TABLE_USERS} u
            LEFT JOIN ${DBHelper.TABLE_FARMER_PROFILES} fp ON u.${DBHelper.COL_ID} = fp.${DBHelper.COL_USER_ID}
            LEFT JOIN ${DBHelper.TABLE_HIRE_REQUESTS} hr ON hr.${DBHelper.COL_FARMER_ID} = u.${DBHelper.COL_ID} 
                AND hr.${DBHelper.COL_LANDOWNER_ID} = $landownerId
            LEFT JOIN ${DBHelper.TABLE_HIRE_WORK} hw ON hr.${DBHelper.COL_ID} = hw.${DBHelper.COL_HIRE_REQUEST_ID}
            WHERE u.${DBHelper.COL_ROLE} = ?
              AND u.${DBHelper.COL_UPAZILA} = ?
            ORDER BY hr.${DBHelper.COL_WORK_DATE} DESC, u.${DBHelper.COL_FULL_NAME} ASC
        """
        try {
            val cursor = DataAccess.executeQuery(requireContext(), sql, arrayOf(DBHelper.ROLE_FARMER, upazillaName ?: ""))
            cursor?.use {
                while(it.moveToNext()){
                    val reqStatus = it.getString(7) ?: ""
                    allFarmers.add(FarmerItem(
                        id = it.getInt(0),
                        name = it.getString(1),
                        phone = it.getString(2),
                        village = it.getString(3),
                        skills = it.getString(4) ?: "No skills added",
                        experience = it.getString(5) ?: "No experience",
                        dailyWage = if(!it.isNull(6)) it.getDouble(6) else 0.0,
                        hasPendingRequest = reqStatus == DBHelper.STATUS_PENDING,
                        selectedDate = it.getString(8),
                        requestStatus = if(reqStatus.isNotEmpty()) reqStatus else null,
                        workStatus = it.getString(9),
                        hireRequestId = if(!it.isNull(10)) it.getInt(10) else null,
                        hireWorkId = if(!it.isNull(11)) it.getInt(11) else null
                    ))
                }
            }
            applyFilter()
        } catch(e: Exception){
            Log.e("LoadFarmers", "Error: ${e.message}")
        }
    }

    private fun applyFilter(){
        val today = LocalDate.now()
        val filtered = when(currentFilter){
            "hire" -> allFarmers.distinctBy { it.id } // Only show unique farmers in hire tab
            "active" -> allFarmers.filter { 
                it.requestStatus == DBHelper.STATUS_PENDING || 
                it.requestStatus == DBHelper.STATUS_ACCEPTED || 
                it.workStatus == DBHelper.STATUS_ON_WORK 
            }
            "completed" -> allFarmers.filter { it.workStatus == DBHelper.STATUS_COMPLETED }
            "cancelled" -> allFarmers.filter { it.requestStatus == DBHelper.STATUS_REJECTED || it.requestStatus == DBHelper.STATUS_CANCELLED }
            "history" -> allFarmers.filter { 
                !it.selectedDate.isNullOrEmpty() && (LocalDate.parse(it.selectedDate).isBefore(today) || it.workStatus == DBHelper.STATUS_COMPLETED)
            }
            else -> allFarmers
        }

        rvFarmers.adapter = FarmerAdapter(
            items = filtered,
            onCardClick = { farmer ->
                (activity as? com.example.villageconnect.landowner.LandownerMainActivity)?.loadFragment(
                    FarmerDetails.newInstance(farmer.id)
                )
            },
            onButtonClick = { farmer ->
                if (currentFilter == "hire") {
                    selectDayAndSendRequest(farmer)
                } else if (farmer.requestStatus == DBHelper.STATUS_PENDING || farmer.requestStatus == DBHelper.STATUS_ACCEPTED) {
                    cancelHireRequest(farmer)
                }
            },
            filterType = currentFilter
        )
    }

    private fun cancelHireRequest(farmer: FarmerItem) {
        val requestId = farmer.hireRequestId ?: return
        val sql = """
            UPDATE ${DBHelper.TABLE_HIRE_REQUESTS} 
            SET ${DBHelper.COL_REQUEST_STATUS} = '${DBHelper.STATUS_CANCELLED}', ${DBHelper.COL_UPDATED_AT} = CURRENT_TIMESTAMP
            WHERE ${DBHelper.COL_ID} = ?
        """
        DataAccess.executeDMLQuery(requireContext(), sql, arrayOf<Any>(requestId))
        loadFarmers()
    }

    private fun selectDayAndSendRequest(farmer: FarmerItem) {
        val next10Days = mutableListOf<LocalDate>()
        val today = LocalDate.now()
        if(LocalTime.now().isBefore(LocalTime.of(7,0))) next10Days.add(today)
        for(i in 1..10) next10Days.add(today.plusDays(i.toLong()))

        // Booked dates by others
        val bookedDates = mutableSetOf<LocalDate>()
        val sqlBooked = "SELECT ${DBHelper.COL_WORK_DATE} FROM ${DBHelper.TABLE_HIRE_REQUESTS} WHERE ${DBHelper.COL_FARMER_ID} = ? AND ${DBHelper.COL_REQUEST_STATUS} = '${DBHelper.STATUS_ACCEPTED}'"
        val cursor = DataAccess.executeQuery(requireContext(), sqlBooked, arrayOf(farmer.id.toString()))
        cursor?.use { while(it.moveToNext()) { try { bookedDates.add(LocalDate.parse(it.getString(0))) } catch(e:Exception){} } }

        // Dates already requested by ME
        val myDates = mutableSetOf<LocalDate>()
        val sqlMy = "SELECT ${DBHelper.COL_WORK_DATE} FROM ${DBHelper.TABLE_HIRE_REQUESTS} WHERE ${DBHelper.COL_FARMER_ID} = ? AND ${DBHelper.COL_LANDOWNER_ID} = ? AND ${DBHelper.COL_REQUEST_STATUS} IN ('${DBHelper.STATUS_PENDING}', '${DBHelper.STATUS_ACCEPTED}')"
        val cursorMy = DataAccess.executeQuery(requireContext(), sqlMy, arrayOf(farmer.id.toString(), landownerId.toString()))
        cursorMy?.use { while(it.moveToNext()) { try { myDates.add(LocalDate.parse(it.getString(0))) } catch(e:Exception){} } }

        val selectable = next10Days.filter { !bookedDates.contains(it) && !myDates.contains(it) }
        
        if (selectable.isEmpty()) {
            android.widget.Toast.makeText(requireContext(), "No available dates", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        DaySliderDialog(requireContext(), selectable.toMutableList(), bookedDates.toList()) { date ->
            sendHireRequest(farmer, date)
            loadFarmers()
        }.show()
    }

    private fun sendHireRequest(farmer: FarmerItem, date: LocalDate) {
        val expiryTime = LocalDateTime.of(date.minusDays(if (date == LocalDate.now()) 0 else 1), LocalTime.of(7, 0))
        val sql = """
            INSERT INTO ${DBHelper.TABLE_HIRE_REQUESTS} 
            (${DBHelper.COL_LANDOWNER_ID}, ${DBHelper.COL_FARMER_ID}, ${DBHelper.COL_WORK_DATE}, ${DBHelper.COL_REQUEST_STATUS}, ${DBHelper.COL_EXPIRES_AT})
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(landowner_id, farmer_id, work_date) DO UPDATE SET
                ${DBHelper.COL_REQUEST_STATUS} = EXCLUDED.${DBHelper.COL_REQUEST_STATUS},
                ${DBHelper.COL_EXPIRES_AT} = EXCLUDED.${DBHelper.COL_EXPIRES_AT},
                ${DBHelper.COL_UPDATED_AT} = CURRENT_TIMESTAMP
        """
        DataAccess.executeDMLQuery(requireContext(), sql, arrayOf<Any>(landownerId, farmer.id, date.toString(), DBHelper.STATUS_PENDING, expiryTime.toString()))
    }
}
