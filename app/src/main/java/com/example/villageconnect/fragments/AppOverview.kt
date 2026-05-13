package com.example.villageconnect.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.villageconnect.R

class AppOverview : Fragment() {

    private lateinit var detailsAgriculture: LinearLayout
    private lateinit var detailsTransport: LinearLayout
    private lateinit var detailsCottage: LinearLayout
    private lateinit var detailsBooth: LinearLayout

    private lateinit var arrowAgriculture: ImageView
    private lateinit var arrowTransport: ImageView
    private lateinit var arrowCottage: ImageView
    private lateinit var arrowBooth: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_app_overview, container, false)

        val btnBack = view.findViewById<TextView>(R.id.btnBack)

        val headerAgriculture = view.findViewById<LinearLayout>(R.id.headerAgriculture)
        val headerTransport = view.findViewById<LinearLayout>(R.id.headerTransport)
        val headerCottage = view.findViewById<LinearLayout>(R.id.headerCottage)
        val headerBooth = view.findViewById<LinearLayout>(R.id.headerBooth)

        detailsAgriculture = view.findViewById(R.id.detailsAgriculture)
        detailsTransport = view.findViewById(R.id.detailsTransport)
        detailsCottage = view.findViewById(R.id.detailsCottage)
        detailsBooth = view.findViewById(R.id.detailsBooth)

        arrowAgriculture = view.findViewById(R.id.arrowAgriculture)
        arrowTransport = view.findViewById(R.id.arrowTransport)
        arrowCottage = view.findViewById(R.id.arrowCottage)
        arrowBooth = view.findViewById(R.id.arrowBooth)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        headerAgriculture.setOnClickListener {
            toggleSection(detailsAgriculture, arrowAgriculture)
        }

        headerTransport.setOnClickListener {
            toggleSection(detailsTransport, arrowTransport)
        }

        headerCottage.setOnClickListener {
            toggleSection(detailsCottage, arrowCottage)
        }

        headerBooth.setOnClickListener {
            toggleSection(detailsBooth, arrowBooth)
        }

        return view
    }

    private fun toggleSection(selectedDetails: LinearLayout, selectedArrow: ImageView) {
        val isAlreadyOpen = selectedDetails.visibility == View.VISIBLE

        closeAllSections()

        if (!isAlreadyOpen) {
            selectedDetails.visibility = View.VISIBLE
            selectedArrow.rotation = 180f
        }
    }

    private fun closeAllSections() {
        detailsAgriculture.visibility = View.GONE
        detailsTransport.visibility = View.GONE
        detailsCottage.visibility = View.GONE
        detailsBooth.visibility = View.GONE

        arrowAgriculture.rotation = 0f
        arrowTransport.rotation = 0f
        arrowCottage.rotation = 0f
        arrowBooth.rotation = 0f
    }
}