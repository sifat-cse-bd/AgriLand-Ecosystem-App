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
import com.example.villageconnect.farmer.adapters.JobOffersAdapter
import com.example.villageconnect.farmer.models.JobOfferItem

class JobOffers : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: JobOffersAdapter
    private val jobOfferList = mutableListOf<JobOfferItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_job_offers, container, false)

        recyclerView = view.findViewById(R.id.rvJobOffers)
        tvEmpty = view.findViewById(R.id.tvJobOfferEmpty)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = JobOffersAdapter(
            jobOfferList,
            onAcceptClick = { acceptRequest(it) },
            onRejectClick = { rejectRequest(it) },
            onItemClick = { /* Optional: handle item click */ }
        )
        recyclerView.adapter = adapter

        loadJobOffers()

        return view
    }

    private fun loadJobOffers() {
        jobOfferList.clear()
        val dbHelper = DBHelper(requireContext())
        val db = dbHelper.readableDatabase

        val farmerId = 1 // Replace with actual logged-in farmer ID
        val statusPending = DBHelper.STATUS_PENDING

        val sql = """
        SELECT hr.${DBHelper.COL_ID} AS requestId,
               u.${DBHelper.COL_ID} AS landownerId,
               u.${DBHelper.COL_FULL_NAME} AS landownerName,
               u.${DBHelper.COL_PHONE} AS phone,
               u.${DBHelper.COL_VILLAGE_NAME} AS village,
               u.${DBHelper.COL_UPAZILA} AS upazila,
               u.${DBHelper.COL_DISTRICT} AS district,
               hr.${DBHelper.COL_WORK_DATE} AS workDate,
               hr.${DBHelper.COL_STATUS} AS status
        FROM ${DBHelper.TABLE_HIRE_REQUESTS} hr
        JOIN ${DBHelper.TABLE_USERS} u
          ON hr.${DBHelper.COL_LANDOWNER_ID} = u.${DBHelper.COL_ID}
        WHERE hr.${DBHelper.COL_FARMER_ID} = ?
          AND hr.${DBHelper.COL_STATUS} = ?
    """.trimIndent()

        val cursor = db.rawQuery(sql, arrayOf(farmerId.toString(), statusPending))

        if (cursor.moveToFirst()) {
            do {
                val item = JobOfferItem(
                    requestId = cursor.getInt(cursor.getColumnIndexOrThrow("requestId")),
                    landownerId = cursor.getInt(cursor.getColumnIndexOrThrow("landownerId")),
                    landownerName = cursor.getString(cursor.getColumnIndexOrThrow("landownerName")),
                    phone = cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                    village = cursor.getString(cursor.getColumnIndexOrThrow("village")),
                    upazila = cursor.getString(cursor.getColumnIndexOrThrow("upazila")),
                    district = cursor.getString(cursor.getColumnIndexOrThrow("district")),
                    workDate = cursor.getString(cursor.getColumnIndexOrThrow("workDate")),
                    status = cursor.getString(cursor.getColumnIndexOrThrow("status"))
                )
                jobOfferList.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()

        tvEmpty.visibility = if (jobOfferList.isEmpty()) View.VISIBLE else View.GONE
        adapter.notifyDataSetChanged()
    }

    private fun acceptRequest(item: JobOfferItem) {
        val dbHelper = DBHelper(requireContext())
        val db = dbHelper.writableDatabase
        db.execSQL(
            "UPDATE ${DBHelper.TABLE_HIRE_REQUESTS} SET ${DBHelper.COL_STATUS} = ? WHERE ${DBHelper.COL_ID} = ?",
            arrayOf(DBHelper.STATUS_ACCEPTED, item.requestId)
        )
        loadJobOffers()
    }

    private fun rejectRequest(item: JobOfferItem) {
        val dbHelper = DBHelper(requireContext())
        val db = dbHelper.writableDatabase
        db.execSQL(
            "UPDATE ${DBHelper.TABLE_HIRE_REQUESTS} SET ${DBHelper.COL_STATUS} = ? WHERE ${DBHelper.COL_ID} = ?",
            arrayOf(DBHelper.STATUS_REJECTED, item.requestId)
        )
        loadJobOffers()
    }
}