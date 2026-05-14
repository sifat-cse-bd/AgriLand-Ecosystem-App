package com.example.villageconnect.landowner.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.villageconnect.R
import com.example.villageconnect.data.DBHelper
import com.example.villageconnect.data.DataAccess
import com.example.villageconnect.landowner.adapters.HistoryAdapter
import com.example.villageconnect.landowner.models.HistoryItem
import com.example.villageconnect.utils.SessionManager
import com.google.android.material.chip.Chip

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private lateinit var chipHire: Chip
    private lateinit var chipService: Chip
    private lateinit var chipOrders: Chip
    private lateinit var rvHistory: RecyclerView

    private var landownerId = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        chipHire = view.findViewById(R.id.chipHire)
        chipService = view.findViewById(R.id.chipService)
        chipOrders = view.findViewById(R.id.chipOrders)
        rvHistory = view.findViewById(R.id.rvHistory)

        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        landownerId = SessionManager(requireContext()).getUserId()

        loadHireHistory()

        chipHire.setOnClickListener { loadHireHistory() }
        chipService.setOnClickListener { loadServiceHistory() }
        chipOrders.setOnClickListener { loadOrderHistory() }
    }

    private fun loadHireHistory() {
        val list = mutableListOf<HistoryItem>()

        val sql = """
            SELECT u.${DBHelper.COL_FULL_NAME},
                   h.${DBHelper.COL_WORK_DATE},
                   h.${DBHelper.COL_STATUS}
            FROM ${DBHelper.TABLE_HIRE_REQUESTS} h
            INNER JOIN ${DBHelper.TABLE_USERS} u
            ON h.${DBHelper.COL_FARMER_ID} = u.${DBHelper.COL_ID}
            WHERE h.${DBHelper.COL_LANDOWNER_ID} = ?
        """

        val cursor = DataAccess.executeQuery(requireContext(), sql, arrayOf(landownerId.toString()))

        cursor?.use {
            while (it.moveToNext()) {
                list.add(
                    HistoryItem(
                        title = "Hire Request",
                        subtitle = "Farmer: ${it.getString(0)} • Work date: ${it.getString(1)}",
                        status = it.getString(2)
                    )
                )
            }
        }

        rvHistory.adapter = HistoryAdapter(list)
    }

    private fun loadServiceHistory() {
        val list = mutableListOf<HistoryItem>()

        val sql = """
            SELECT ${DBHelper.COL_SERVICE_TYPE},
                   ${DBHelper.COL_QUEUE_NO},
                   ${DBHelper.COL_STATUS}
            FROM ${DBHelper.TABLE_SERVICE_BOOKINGS}
            WHERE ${DBHelper.COL_LANDOWNER_ID} = ?
        """

        val cursor = DataAccess.executeQuery(requireContext(), sql, arrayOf(landownerId.toString()))

        cursor?.use {
            while (it.moveToNext()) {
                list.add(
                    HistoryItem(
                        title = "Service Booking",
                        subtitle = "${it.getString(0)} • Queue No: ${it.getInt(1)}",
                        status = it.getString(2)
                    )
                )
            }
        }

        rvHistory.adapter = HistoryAdapter(list)
    }

    private fun loadOrderHistory() {
        val list = mutableListOf<HistoryItem>()

        val sql = """
            SELECT i.${DBHelper.COL_ITEM_NAME},
                   o.${DBHelper.COL_QUANTITY},
                   o.${DBHelper.COL_TOTAL_PRICE},
                   o.${DBHelper.COL_STATUS}
            FROM ${DBHelper.TABLE_INVENTORY_ORDERS} o
            INNER JOIN ${DBHelper.TABLE_INVENTORY} i
            ON o.${DBHelper.COL_INVENTORY_ID} = i.${DBHelper.COL_ID}
            WHERE o.${DBHelper.COL_LANDOWNER_ID} = ?
        """

        val cursor = DataAccess.executeQuery(requireContext(), sql, arrayOf(landownerId.toString()))

        cursor?.use {
            while (it.moveToNext()) {
                list.add(
                    HistoryItem(
                        title = "Market Order",
                        subtitle = "${it.getString(0)} • Qty: ${it.getInt(1)} • ৳${it.getDouble(2)}",
                        status = it.getString(3)
                    )
                )
            }
        }

        rvHistory.adapter = HistoryAdapter(list)
    }
}