package com.example.villageconnect.landowner.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
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
    private var villageName: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvFarmers = view.findViewById(R.id.rvFarmers)
        rvFarmers.layoutManager = LinearLayoutManager(requireContext())

        landownerId = SessionManager(requireContext()).getUserId()
        villageName = getLandownerVillage()

        loadFarmers()
    }

    private fun getLandownerVillage(): String? {
        val sql = """
            SELECT ${DBHelper.COL_VILLAGE_NAME}
            FROM ${DBHelper.TABLE_USERS}
            WHERE ${DBHelper.COL_ID} = ?
        """

        val cursor = DataAccess.executeQuery(requireContext(), sql, arrayOf(landownerId.toString()))

        var village: String? = null

        cursor?.use {
            if (it.moveToFirst()) {
                village = it.getString(0)
            }
        }

        return village
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
            AND u.${DBHelper.COL_VILLAGE_NAME} = ?
        """

        val cursor = DataAccess.executeQuery(
            requireContext(),
            sql,
            arrayOf(DBHelper.ROLE_FARMER, villageName ?: "")
        )

        cursor?.use {
            while (it.moveToNext()) {
                farmers.add(
                    FarmerItem(
                        id = it.getInt(0),
                        name = it.getString(1),
                        phone = it.getString(2),
                        village = it.getString(3),
                        skills = it.getString(4),
                        experience = it.getString(5),
                        dailyWage = if (!it.isNull(6)) it.getDouble(6) else 0.0
                    )
                )
            }
        }

        rvFarmers.adapter = FarmerAdapter(farmers) { farmer ->
            val fragment = FarmerDetails.newInstance(farmer.id)
            (requireActivity() as LandownerMainActivity).loadFragment(fragment)
        }
    }
}