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
import com.example.villageconnect.landowner.adapters.DashboardAdapter
import com.example.villageconnect.landowner.models.DashboardItem
import com.example.villageconnect.utils.SessionManager
import java.time.LocalTime
import java.util.Calendar

class LandownerDashboard : Fragment(R.layout.fragment_landowner_dashboard) {

    private lateinit var tvGreeting: TextView
    private lateinit var tvLandownerName: TextView
    private lateinit var tvVillage: TextView
    private lateinit var tvFarmerCount: TextView
    private lateinit var tvServiceCount: TextView
    private lateinit var tvOrderCount: TextView
    private lateinit var rvDashboardCards: RecyclerView

    private var landownerId = -1
    private var villageName: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tvGreeting = view.findViewById(R.id.tvGreeting)
        tvLandownerName = view.findViewById(R.id.tvLandownerName)
        tvVillage = view.findViewById(R.id.tvVillage)
        tvFarmerCount = view.findViewById(R.id.tvFarmerCount)
        tvServiceCount = view.findViewById(R.id.tvServiceCount)
        tvOrderCount = view.findViewById(R.id.tvOrderCount)
        rvDashboardCards = view.findViewById(R.id.rvDashboardCards)

        landownerId = SessionManager(requireContext()).getUserId()

        loadLandownerInfo()
        loadCounts()
        setupDashboardCards()
    }

    private fun loadLandownerInfo() {
        val sql = """
            SELECT ${DBHelper.COL_FULL_NAME}, ${DBHelper.COL_VILLAGE_NAME}
            FROM ${DBHelper.TABLE_USERS}
            WHERE ${DBHelper.COL_ID} = ?
            LIMIT 1
        """

        val cursor = DataAccess.executeQuery(
            requireContext(),
            sql,
            arrayOf(landownerId.toString())
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val name = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_FULL_NAME))
                villageName = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_VILLAGE_NAME))

                tvGreeting.text = getGreeting()
                tvLandownerName.text = name
                tvVillage.text = "Village: ${villageName ?: "Not updated"}"
            }
        }
    }

    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when(hour)
        {
            in 5..11 -> "Good Morning,"
            in 12..16 -> "Good Afternoon,"
            in 17..20 -> "Good Evening,"
            else -> "Good Night,"
        }
    }

    private fun loadCounts() {
        tvFarmerCount.text = getCount(
            """
            SELECT COUNT(*) FROM ${DBHelper.TABLE_USERS}
            WHERE ${DBHelper.COL_ROLE} = ? AND ${DBHelper.COL_VILLAGE_NAME} = ?
            """,
            arrayOf(DBHelper.ROLE_FARMER, villageName ?: "")
        ).toString()

        tvServiceCount.text = getCount(
            """
            SELECT COUNT(*) FROM ${DBHelper.TABLE_SERVICE_BOOKINGS}
            WHERE ${DBHelper.COL_LANDOWNER_ID} = ?
            """,
            arrayOf(landownerId.toString())
        ).toString()

        tvOrderCount.text = getCount(
            """
            SELECT COUNT(*) FROM ${DBHelper.TABLE_INVENTORY_ORDERS}
            WHERE ${DBHelper.COL_LANDOWNER_ID} = ?
            """,
            arrayOf(landownerId.toString())
        ).toString()
    }

    private fun getCount(sql: String, args: Array<String>): Int {
        var count = 0
        val cursor = DataAccess.executeQuery(requireContext(), sql, args)
        cursor?.use {
            if (it.moveToFirst()) count = it.getInt(0)
        }
        return count
    }

    private fun setupDashboardCards() {
        val items = listOf(
            DashboardItem("👨‍🌾", "Find Farmers", "Hire local farmers from your village"),
            DashboardItem("🚜", "Book Agri Service", "Book tractor, pump and field service"),
            DashboardItem("🌱", "Agri Market", "Buy seeds, fertilizer and tools"),
            DashboardItem("📋", "My Activity", "View hire, booking and order history")
        )

        rvDashboardCards.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvDashboardCards.adapter = DashboardAdapter(items) { item ->
            val activity = requireActivity() as LandownerMainActivity

            when (item.title) {
                "Find Farmers" -> activity.loadFragment(FarmerListFragment())
                "Book Agri Service" -> activity.loadFragment(MerchantListFragment())
                "Agri Market" -> activity.loadFragment(InventoryListFragment())
                "My Activity" -> activity.loadFragment(HistoryFragment())
            }
        }
    }
}