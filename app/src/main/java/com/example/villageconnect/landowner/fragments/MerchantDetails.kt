package com.example.villageconnect.landowner.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.villageconnect.R
import com.example.villageconnect.data.DBHelper
import com.example.villageconnect.data.DataAccess
import com.example.villageconnect.landowner.adapters.AssetAdapter
import com.example.villageconnect.landowner.models.AssetItem
import com.example.villageconnect.utils.SessionManager
import com.google.android.material.button.MaterialButton

class MerchantDetails : Fragment(R.layout.fragment_merchant_details) {

    private lateinit var tvName: TextView
    private lateinit var tvAddress: TextView
    private lateinit var rvAssets: RecyclerView
    private lateinit var edtServiceType: EditText
    private lateinit var btnBookService: MaterialButton

    private var merchantId = -1
    private var landownerId = -1
    private var selectedAssetId = -1

    companion object {
        fun newInstance(merchantId: Int): MerchantDetails {
            val fragment = MerchantDetails()
            val bundle = Bundle()
            bundle.putInt("merchant_id", merchantId)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tvName = view.findViewById(R.id.tvMerchantDetailsName)
        tvAddress = view.findViewById(R.id.tvMerchantDetailsAddress)
        rvAssets = view.findViewById(R.id.rvMerchantAssets)
        edtServiceType = view.findViewById(R.id.edtServiceType)
        btnBookService = view.findViewById(R.id.btnBookService)

        merchantId = arguments?.getInt("merchant_id") ?: -1
        landownerId = SessionManager(requireContext()).getUserId()

        rvAssets.layoutManager = LinearLayoutManager(requireContext())

        loadMerchantDetails()
        loadAssets()

        btnBookService.setOnClickListener {
            bookService()
        }
    }

    private fun loadMerchantDetails() {
        val sql = """
            SELECT ${DBHelper.COL_FULL_NAME}, ${DBHelper.COL_VILLAGE_NAME}, ${DBHelper.COL_UPAZILA}, ${DBHelper.COL_DISTRICT}
            FROM ${DBHelper.TABLE_USERS}
            WHERE ${DBHelper.COL_ID} = ?
        """

        val cursor = DataAccess.executeQuery(requireContext(), sql, arrayOf(merchantId.toString()))

        cursor?.use {
            if (it.moveToFirst()) {
                tvName.text = it.getString(0)
                tvAddress.text = "${it.getString(1) ?: ""}, ${it.getString(2) ?: ""}, ${it.getString(3) ?: ""}"
            }
        }
    }

    private fun loadAssets() {
        val assets = mutableListOf<AssetItem>()

        val sql = """
            SELECT ${DBHelper.COL_ID}, ${DBHelper.COL_MERCHANT_ID}, ${DBHelper.COL_ASSET_TYPE}, ${DBHelper.COL_STATUS}, ${DBHelper.COL_TOTAL_CAPACITY}
            FROM ${DBHelper.TABLE_MERCHANT_ASSETS}
            WHERE ${DBHelper.COL_MERCHANT_ID} = ?
        """

        val cursor = DataAccess.executeQuery(requireContext(), sql, arrayOf(merchantId.toString()))

        cursor?.use {
            while (it.moveToNext()) {
                assets.add(
                    AssetItem(
                        id = it.getInt(0),
                        merchantId = it.getInt(1),
                        assetType = it.getString(2),
                        status = it.getString(3),
                        totalCapacity = if (!it.isNull(4)) it.getDouble(4) else 0.0
                    )
                )
            }
        }

        rvAssets.adapter = AssetAdapter(assets) { asset ->
            selectedAssetId = asset.id
            edtServiceType.setText(asset.assetType)
        }
    }

    private fun bookService() {
        val serviceType = edtServiceType.text.toString().trim()

        if (selectedAssetId == -1) {
            Toast.makeText(requireContext(), "Select an asset", Toast.LENGTH_SHORT).show()
            return
        }

        if (serviceType.isEmpty()) {
            Toast.makeText(requireContext(), "Enter service type", Toast.LENGTH_SHORT).show()
            return
        }

        val queueNo = getNextQueueNo()

        val sql = """
            INSERT INTO ${DBHelper.TABLE_SERVICE_BOOKINGS}
            (${DBHelper.COL_LANDOWNER_ID}, ${DBHelper.COL_MERCHANT_ID}, ${DBHelper.COL_ASSET_ID}, ${DBHelper.COL_SERVICE_TYPE}, ${DBHelper.COL_QUEUE_NO}, ${DBHelper.COL_STATUS})
            VALUES (?, ?, ?, ?, ?, ?)
        """

        val result = DataAccess.executeDMLQuery(
            requireContext(),
            sql,
            arrayOf(landownerId, merchantId, selectedAssetId, serviceType, queueNo, DBHelper.STATUS_PENDING)
        )

        if (result) {
            Toast.makeText(requireContext(), "Service booked. Queue No: $queueNo", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Booking failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getNextQueueNo(): Int {
        val sql = """
            SELECT COUNT(*) + 1
            FROM ${DBHelper.TABLE_SERVICE_BOOKINGS}
            WHERE ${DBHelper.COL_MERCHANT_ID} = ?
            AND ${DBHelper.COL_STATUS} != ?
        """

        val cursor = DataAccess.executeQuery(
            requireContext(),
            sql,
            arrayOf(merchantId.toString(), DBHelper.STATUS_COMPLETED)
        )

        var nextQueue = 1

        cursor?.use {
            if (it.moveToFirst()) {
                nextQueue = it.getInt(0)
            }
        }

        return nextQueue
    }
}