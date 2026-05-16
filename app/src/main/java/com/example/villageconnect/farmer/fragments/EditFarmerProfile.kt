package com.example.villageconnect.farmer.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.villageconnect.R
import com.google.android.material.button.MaterialButtonToggleGroup

class EditFarmerProfile : Fragment() {
    private lateinit var toggleGroup: MaterialButtonToggleGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_farmer_profile, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toggleGroup = view.findViewById(R.id.availabilityToggleGroup)


        val initialSelectedId = toggleGroup.checkedButtonId
        val initialSelection = when (initialSelectedId) {
            R.id.btnAvailable -> "Available"
            R.id.btnBusy -> "Busy"
            R.id.btnOnLeave -> "On Leave"
            else -> "None"
        }
        Log.d("FarmerEdit", "Initial availability: $initialSelection")


        toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                val selected = when (checkedId) {
                    R.id.btnAvailable -> "Available"
                    R.id.btnBusy -> "Busy"
                    R.id.btnOnLeave -> "On Leave"
                    else -> ""
                }
                Log.d("FarmerEdit", "Selected availability: $selected")
            }
        }
    }
}