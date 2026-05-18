package com.example.villageconnect.farmer.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.villageconnect.R
import com.example.villageconnect.data.DBHelper
import com.example.villageconnect.data.DataAccess
import com.example.villageconnect.farmer.adapters.JobOffersAdapter
import com.example.villageconnect.farmer.models.JobOfferItem
import com.example.villageconnect.utils.SessionManager

class JobOffersList : Fragment(R.layout.fragment_job_offers_list) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: JobOffersAdapter
    private val jobOfferList = mutableListOf<JobOfferItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvJobOffersList)
        tvEmpty = view.findViewById(R.id.tvJobOffersListEmpty)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = JobOffersAdapter(
            jobOfferList,
            onAcceptClick = { acceptRequest(it) },
            onRejectClick = { rejectRequest(it) },
            onItemClick = { /* Optional: handle item click */ }
        )
        recyclerView.adapter = adapter

        loadJobOffers()
    }

    private fun loadJobOffers() {
        jobOfferList.clear()
        val farmerId = SessionManager(requireContext()).getUserId()

        // Load all offers: Pending, Accepted, Rejected (history)
        val sql = """
        SELECT hr.${DBHelper.COL_ID} AS requestId,
               u.${DBHelper.COL_ID} AS landownerId,
               u.${DBHelper.COL_FULL_NAME} AS landownerName,
               u.${DBHelper.COL_PHONE} AS phone,
               u.${DBHelper.COL_VILLAGE_NAME} AS village,
               u.${DBHelper.COL_UPAZILA} AS upazila,
               u.${DBHelper.COL_DISTRICT} AS district,
               hr.${DBHelper.COL_WORK_DATE} AS workDate,
               hr.${DBHelper.COL_REQUEST_STATUS} AS status
        FROM ${DBHelper.TABLE_HIRE_REQUESTS} hr
        JOIN ${DBHelper.TABLE_USERS} u
          ON hr.${DBHelper.COL_LANDOWNER_ID} = u.${DBHelper.COL_ID}
        WHERE hr.${DBHelper.COL_FARMER_ID} = ?
        ORDER BY hr.${DBHelper.COL_CREATED_AT} DESC
    """.trimIndent()

        val cursor = DataAccess.executeQuery(requireContext(), sql, arrayOf(farmerId.toString()))

        cursor?.use {
            while(it.moveToNext()) {
                jobOfferList.add(JobOfferItem(
                    requestId = it.getInt(0),
                    landownerId = it.getInt(1),
                    landownerName = it.getString(2),
                    phone = it.getString(3),
                    village = it.getString(4),
                    upazila = it.getString(5),
                    district = it.getString(6),
                    workDate = it.getString(7),
                    status = it.getString(8)
                ))
            }
        }

        tvEmpty.visibility = if(jobOfferList.isEmpty()) View.VISIBLE else View.GONE
        adapter.notifyDataSetChanged()
    }

    private fun acceptRequest(item: JobOfferItem) {
        val sql = """
            UPDATE ${DBHelper.TABLE_HIRE_REQUESTS} 
            SET ${DBHelper.COL_REQUEST_STATUS} = ?, ${DBHelper.COL_UPDATED_AT} = CURRENT_TIMESTAMP 
            WHERE ${DBHelper.COL_ID} = ?
        """
        DataAccess.executeDMLQuery(
            requireContext(),
            sql,
            arrayOf(DBHelper.STATUS_ACCEPTED, item.requestId)
        )
        loadJobOffers()
    }

    private fun rejectRequest(item: JobOfferItem) {
        val sql = """
            UPDATE ${DBHelper.TABLE_HIRE_REQUESTS} 
            SET ${DBHelper.COL_REQUEST_STATUS} = ?, ${DBHelper.COL_UPDATED_AT} = CURRENT_TIMESTAMP 
            WHERE ${DBHelper.COL_ID} = ?
        """
        DataAccess.executeDMLQuery(
            requireContext(),
            sql,
            arrayOf(DBHelper.STATUS_REJECTED, item.requestId)
        )
        loadJobOffers()
    }
}