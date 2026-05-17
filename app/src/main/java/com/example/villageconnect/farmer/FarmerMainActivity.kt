package com.example.villageconnect.farmer

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.villageconnect.R
import com.example.villageconnect.farmer.fragments.FarmerDashboard
import com.example.villageconnect.farmer.fragments.JobOffers
import com.example.villageconnect.farmer.fragments.MyJobs
import com.google.android.material.bottomnavigation.BottomNavigationView

class FarmerMainActivity : AppCompatActivity() {
    lateinit var bottomNavigation: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_farmer_main)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        loadFragment(FarmerDashboard())

        bottomNavigation = findViewById(R.id.farmerBottomNavigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId)
            {
                R.id.navHome-> {
                   loadFragment(FarmerDashboard())
                    true
                }
                R.id.navJobOffers-> {
                    loadFragment(JobOffers())
                    true
                }
                R.id.navMyJobs-> {
                    loadFragment(MyJobs())
                    true
                }
                R.id.navProfile-> {
                    loadFragment(FarmerProfile())
                    true
                }
                else -> false
            }


        }
    }

    fun loadFragment(fragment: Fragment)
    {
        supportFragmentManager.beginTransaction()
            .replace(R.id.farmerFragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}