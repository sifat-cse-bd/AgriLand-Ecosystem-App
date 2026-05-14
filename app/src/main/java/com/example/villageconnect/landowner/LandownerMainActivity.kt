package com.example.villageconnect.landowner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.villageconnect.R
import com.example.villageconnect.landowner.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class LandownerMainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landowner_main)

        bottomNavigation = findViewById(R.id.bottomNavigation)

        loadFragment(LandownerDashboardFragment())

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navDashboard -> {
                    loadFragment(LandownerDashboardFragment())
                    true
                }

                R.id.navFarmers -> {
                    loadFragment(FarmerListFragment())
                    true
                }

                R.id.navMerchants -> {
                    loadFragment(MerchantListFragment())
                    true
                }

                R.id.navMarket -> {
                    loadFragment(InventoryListFragment())
                    true
                }

                R.id.navProfile -> {
                    loadFragment(LandownerProfileFragment())
                    true
                }

                else -> false
            }
        }
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.landownerFragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}