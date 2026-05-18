package com.example.villageconnect.farmer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.villageconnect.R
import com.example.villageconnect.data.DBHelper
import com.example.villageconnect.data.DataAccess
import com.example.villageconnect.farmer.adapters.MyJobAdapter
import com.example.villageconnect.farmer.models.MyJobItem
import com.example.villageconnect.utils.SessionManager
import com.example.villageconnect.utils.WorkStatusManager
import com.google.android.material.tabs.TabLayout
import java.time.LocalDate

class MyJobs : Fragment(R.layout.fragment_my_jobs) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: MyJobAdapter
    private val jobList = mutableListOf<MyJobItem>()
    private var farmerId = -1
    private var currentTab = 0 // 0: Hired, 1: Active, 2: Completed

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvMyJobs)
        tvEmpty = view.findViewById(R.id.tvMyJobsEmpty)
        tabLayout = view.findViewById(R.id.tabLayoutMyJobs)

        farmerId = SessionManager(requireContext()).getUserId()

        // Check and update any expired/incomplete statuses
        WorkStatusManager.checkAndUpdateStatuses(requireContext())

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = MyJobAdapter(
            jobList,
            onStartWorkClick = { startWork(it) },
            onCompleteClick = { completeWork(it) },
            onCancelClick = { cancelWork(it) }
        )
        recyclerView.adapter = adapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                loadMyJobs()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        loadMyJobs()
    }

    private fun loadMyJobs() {
        jobList.clear()

        val sql = when (currentTab) {
            0 -> """
                SELECT hr.${DBHelper.COL_ID}, u.${DBHelper.COL_FULL_NAME}, u.${DBHelper.COL_PHONE}, 
                       u.${DBHelper.COL_VILLAGE_NAME}, hr.${DBHelper.COL_WORK_DATE}, 
                       hr.${DBHelper.COL_REQUEST_STATUS}, COALESCE(hw.${DBHelper.COL_WORK_STATUS}, '${DBHelper.STATUS_PENDING}'),
                       hw.${DBHelper.COL_ID}
                FROM ${DBHelper.TABLE_HIRE_REQUESTS} hr
                JOIN ${DBHelper.TABLE_USERS} u ON hr.${DBHelper.COL_LANDOWNER_ID} = u.${DBHelper.COL_ID}
                LEFT JOIN ${DBHelper.TABLE_HIRE_WORK} hw ON hr.${DBHelper.COL_ID} = hw.${DBHelper.COL_HIRE_REQUEST_ID}
                WHERE hr.${DBHelper.COL_FARMER_ID} = ?
                  AND hr.${DBHelper.COL_REQUEST_STATUS} = '${DBHelper.STATUS_ACCEPTED}'
                  AND (hw.${DBHelper.COL_WORK_STATUS} IS NULL OR hw.${DBHelper.COL_WORK_STATUS} = '${DBHelper.STATUS_PENDING}')
                ORDER BY hr.${DBHelper.COL_WORK_DATE} ASC
            """
            1 -> """
                SELECT hr.${DBHelper.COL_ID}, u.${DBHelper.COL_FULL_NAME}, u.${DBHelper.COL_PHONE}, 
                       u.${DBHelper.COL_VILLAGE_NAME}, hr.${DBHelper.COL_WORK_DATE}, 
                       hr.${DBHelper.COL_REQUEST_STATUS}, hw.${DBHelper.COL_WORK_STATUS},
                       hw.${DBHelper.COL_ID}
                FROM ${DBHelper.TABLE_HIRE_REQUESTS} hr
                JOIN ${DBHelper.TABLE_USERS} u ON hr.${DBHelper.COL_LANDOWNER_ID} = u.${DBHelper.COL_ID}
                JOIN ${DBHelper.TABLE_HIRE_WORK} hw ON hr.${DBHelper.COL_ID} = hw.${DBHelper.COL_HIRE_REQUEST_ID}
                WHERE hr.${DBHelper.COL_FARMER_ID} = ?
                  AND hw.${DBHelper.COL_WORK_STATUS} = '${DBHelper.STATUS_ON_WORK}'
                ORDER BY hr.${DBHelper.COL_WORK_DATE} ASC
            """
            else -> """
                SELECT hr.${DBHelper.COL_ID}, u.${DBHelper.COL_FULL_NAME}, u.${DBHelper.COL_PHONE}, 
                       u.${DBHelper.COL_VILLAGE_NAME}, hr.${DBHelper.COL_WORK_DATE}, 
                       hr.${DBHelper.COL_REQUEST_STATUS}, hw.${DBHelper.COL_WORK_STATUS},
                       hw.${DBHelper.COL_ID}
                FROM ${DBHelper.TABLE_HIRE_REQUESTS} hr
                JOIN ${DBHelper.TABLE_USERS} u ON hr.${DBHelper.COL_LANDOWNER_ID} = u.${DBHelper.COL_ID}
                JOIN ${DBHelper.TABLE_HIRE_WORK} hw ON hr.${DBHelper.COL_ID} = hw.${DBHelper.COL_HIRE_REQUEST_ID}
                WHERE hr.${DBHelper.COL_FARMER_ID} = ?
                  AND hw.${DBHelper.COL_WORK_STATUS} = '${DBHelper.STATUS_COMPLETED}'
                ORDER BY hr.${DBHelper.COL_WORK_DATE} DESC
            """
        }.trimIndent()

        val cursor = DataAccess.executeQuery(
            requireContext(),
            sql,
            arrayOf(farmerId.toString())
        )

        cursor?.use {
            while(it.moveToNext()) {
                jobList.add(MyJobItem(
                    hireRequestId = it.getInt(0),
                    landownerName = it.getString(1),
                    phone = it.getString(2),
                    village = it.getString(3),
                    workDate = it.getString(4),
                    requestStatus = it.getString(5),
                    workStatus = it.getString(6),
                    hireWorkId = if(!it.isNull(7)) it.getInt(7) else null
                ))
            }
        }

        tvEmpty.visibility = if(jobList.isEmpty()) View.VISIBLE else View.GONE
        adapter.notifyDataSetChanged()
    }

    private fun startWork(item: MyJobItem) {
        // Check if hire_work record exists
        val checkSql = "SELECT COUNT(*) FROM ${DBHelper.TABLE_HIRE_WORK} WHERE ${DBHelper.COL_HIRE_REQUEST_ID} = ?"
        val exists = DataAccess.executeScalarInt(requireContext(), checkSql, arrayOf(item.hireRequestId.toString())) > 0

        if (exists) {
            val updateSql = """
                UPDATE ${DBHelper.TABLE_HIRE_WORK}
                SET ${DBHelper.COL_WORK_STATUS} = ?, started_at = CURRENT_TIMESTAMP
                WHERE ${DBHelper.COL_HIRE_REQUEST_ID} = ?
            """
            DataAccess.executeDMLQuery(requireContext(), updateSql, arrayOf(DBHelper.STATUS_ON_WORK, item.hireRequestId))
        } else {
            val insertSql = """
                INSERT INTO ${DBHelper.TABLE_HIRE_WORK} (${DBHelper.COL_HIRE_REQUEST_ID}, ${DBHelper.COL_WORK_STATUS}, started_at)
                VALUES (?, ?, CURRENT_TIMESTAMP)
            """
            DataAccess.executeDMLQuery(requireContext(), insertSql, arrayOf(item.hireRequestId, DBHelper.STATUS_ON_WORK))
        }
        loadMyJobs()
    }

    private fun completeWork(item: MyJobItem) {
        val sql = """
            UPDATE ${DBHelper.TABLE_HIRE_WORK}
            SET ${DBHelper.COL_WORK_STATUS} = ?, completed_at = CURRENT_TIMESTAMP
            WHERE ${DBHelper.COL_HIRE_REQUEST_ID} = ?
        """
        DataAccess.executeDMLQuery(requireContext(), sql, arrayOf(DBHelper.STATUS_COMPLETED, item.hireRequestId))
        loadMyJobs()
    }

    private fun cancelWork(item: MyJobItem) {
        // 2-day notice rule: Can only cancel if work_date is 2+ days away
        val workDate = LocalDate.parse(item.workDate)
        val today = LocalDate.now()
        val daysUntilWork = java.time.temporal.ChronoUnit.DAYS.between(today, workDate)

        if(daysUntilWork < 2) {
            android.widget.Toast.makeText(
                requireContext(),
                "Cannot cancel within 2 days of work date",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }

        val sql = """
            UPDATE ${DBHelper.TABLE_HIRE_REQUESTS}
            SET ${DBHelper.COL_REQUEST_STATUS} = ?, ${DBHelper.COL_UPDATED_AT} = CURRENT_TIMESTAMP
            WHERE ${DBHelper.COL_ID} = ?
        """
        DataAccess.executeDMLQuery(
            requireContext(),
            sql,
            arrayOf(DBHelper.STATUS_REJECTED, item.hireRequestId)
        )
        loadMyJobs()
    }
}