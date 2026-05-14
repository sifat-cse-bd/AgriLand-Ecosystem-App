package com.example.villageconnect.landowner.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.villageconnect.R
import com.example.villageconnect.data.DBHelper
import com.example.villageconnect.data.DataAccess
import com.example.villageconnect.landowner.LandownerMainActivity
import com.example.villageconnect.landowner.adapters.MerchantAdapter
import com.example.villageconnect.landowner.models.MerchantItem
import com.example.villageconnect.utils.SessionManager

class MerchantList : Fragment(R.layout.fragment_merchant_list) {

    private lateinit var rvMerchants: RecyclerView
    private var landownerId = -1
    private var villageName: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvMerchants = view.findViewById(R.id.rvMerchants)
        rvMerchants.layoutManager = LinearLayoutManager(requireContext())

        landownerId = SessionManager(requireContext()).getUserId()
        villageName = getLandownerVillage()

        loadMerchants()
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
            if (it.moveToFirst()) village = it.getString(0)
        }

        return village
    }

    private fun loadMerchants() {
        val merchants = mutableListOf<MerchantItem>()

        val sql = """
            SELECT ${DBHelper.COL_ID}, ${DBHelper.COL_FULL_NAME}, ${DBHelper.COL_PHONE}, ${DBHelper.COL_VILLAGE_NAME}
            FROM ${DBHelper.TABLE_USERS}
            WHERE ${DBHelper.COL_ROLE} = ?
            AND ${DBHelper.COL_VILLAGE_NAME} = ?
        """

        val cursor = DataAccess.executeQuery(
            requireContext(),
            sql,
            arrayOf(DBHelper.ROLE_MERCHANT, villageName ?: "")
        )

        cursor?.use {
            while (it.moveToNext()) {
                merchants.add(
                    MerchantItem(
                        id = it.getInt(0),
                        name = it.getString(1),
                        phone = it.getString(2),
                        village = it.getString(3)
                    )
                )
            }
        }

        rvMerchants.adapter = MerchantAdapter(merchants) { merchant ->
            val fragment = MerchantDetails.newInstance(merchant.id)
            (requireActivity() as LandownerMainActivity).loadFragment(fragment)
        }
    }
}
