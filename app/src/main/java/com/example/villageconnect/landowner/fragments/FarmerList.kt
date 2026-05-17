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

class FarmerList() : Fragment(R.layout.fragment_farmer_list) {

    private lateinit var rvFarmers: RecyclerView
    private var landownerId = -1
    private var upazillaName: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvFarmers = view.findViewById(R.id.rvFarmers)
        rvFarmers.layoutManager = LinearLayoutManager(requireContext())

        landownerId = SessionManager(requireContext()).getUserId()
        upazillaName = getLandownerUpazilla()

        loadFarmers()
    }

    private fun getLandownerUpazilla(): String? {
        val sql = """
            SELECT ${DBHelper.COL_UPAZILA}
            FROM ${DBHelper.TABLE_USERS}
            WHERE ${DBHelper.COL_ID} = ?
        """
        val cursor = DataAccess.executeQuery(requireContext(), sql, arrayOf(landownerId.toString()))
        var upazilla: String? = null
        cursor?.use {
            if (it.moveToFirst()) {
                upazilla = it.getString(0)
            }
        }
        return upazilla
    }

    private fun loadFarmers() {
        val farmers = mutableListOf<FarmerItem>()
        val sql = """
            SELECT u.${DBHelper.COL_ID},
                   u.${DBHelper.COL_FULL_NAME},
                   u.${DBHelper.COL_PHONE},
                   u.${DBHelper.COL_VILLAGE_NAME},
                   fp.${DBHelper.COL_SKILLS},
                   fp.${DBHelper.COL_EXPERIENCE},
                   fp.${DBHelper.COL_DAILY_WAGE}
            FROM ${DBHelper.TABLE_USERS} u
            LEFT JOIN ${DBHelper.TABLE_FARMER_PROFILES} fp
            ON u.${DBHelper.COL_ID} = fp.${DBHelper.COL_USER_ID}
            WHERE u.${DBHelper.COL_ROLE} = ?
            AND u.${DBHelper.COL_UPAZILA} = ?
        """.trimIndent()

        try {
            val cursor = DataAccess.executeQuery(requireContext(), sql, arrayOf(DBHelper.ROLE_FARMER, upazillaName ?: ""))
            cursor?.use {
                while (it.moveToNext()) {
                    farmers.add(
                        FarmerItem(
                            id = it.getInt(0),
                            name = it.getString(1),
                            phone = it.getString(2),
                            village = it.getString(3),
                            skills = it.getString(4) ?: "No skills added",
                            experience = it.getString(5) ?: "No experience",
                            dailyWage = if (!it.isNull(6)) it.getDouble(6) else 0.0,
                            hasPendingRequest = checkPendingRequest(it.getInt(0))
                        )
                    )
                }
            }

            rvFarmers.adapter = FarmerAdapter(
                farmers,
                onCardClick = { farmer ->
                    val fragment = FarmerDetails.newInstance(farmer.id)
                    (requireActivity() as LandownerMainActivity).loadFragment(fragment)
                },
                onButtonClick = { farmer ->
                    if (farmer.hasPendingRequest) {
                        cancelHireRequest(farmer.id)
                        farmer.hasPendingRequest = false
                    } else {
                        sendHireRequest(farmer.id)
                        farmer.hasPendingRequest = true
                    }
                    rvFarmers.adapter?.notifyDataSetChanged()
                }
            )

        } catch (e: Exception) {
            Log.e("LoadFarmers_Error", "Error loading farmers data: ${e.message}", e)
        }
    }

    private fun checkPendingRequest(farmerId: Int): Boolean {
        val sql = """
            SELECT COUNT(*) FROM ${DBHelper.TABLE_HIRE_REQUESTS}
            WHERE ${DBHelper.COL_LANDOWNER_ID} = ?
              AND ${DBHelper.COL_FARMER_ID} = ?
              AND ${DBHelper.COL_STATUS} = '${DBHelper.STATUS_PENDING}'
        """
        val cursor = DataAccess.executeQuery(requireContext(), sql, arrayOf(landownerId.toString(), farmerId.toString()))
        var count = 0
        cursor?.use {
            if (it.moveToFirst()) count = it.getInt(0)
        }
        return count > 0
    }

    private fun sendHireRequest(farmerId: Int) {
        val sql = """
            INSERT INTO ${DBHelper.TABLE_HIRE_REQUESTS}
            (${DBHelper.COL_LANDOWNER_ID}, ${DBHelper.COL_FARMER_ID}, ${DBHelper.COL_STATUS})
            VALUES (?, ?, ?)
        """
        DataAccess.executeDMLQuery(requireContext(), sql, arrayOf(landownerId, farmerId, DBHelper.STATUS_PENDING))
    }

    private fun cancelHireRequest(farmerId: Int) {
        val sql = """
            DELETE FROM ${DBHelper.TABLE_HIRE_REQUESTS}
            WHERE ${DBHelper.COL_LANDOWNER_ID} = ?
              AND ${DBHelper.COL_FARMER_ID} = ?
              AND ${DBHelper.COL_STATUS} = '${DBHelper.STATUS_PENDING}'
        """
        DataAccess.executeDMLQuery(requireContext(), sql, arrayOf(landownerId, farmerId))
    }
}