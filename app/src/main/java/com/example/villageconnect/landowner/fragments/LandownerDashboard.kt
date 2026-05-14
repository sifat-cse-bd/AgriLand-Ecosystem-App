package com.example.villageconnect.landowner.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.villageconnect.BuildConfig
import com.example.villageconnect.R
import com.example.villageconnect.data.DBHelper
import com.example.villageconnect.data.DataAccess
import com.example.villageconnect.data.weather.WeatherRetrofitClient
import com.example.villageconnect.landowner.LandownerMainActivity
import com.example.villageconnect.landowner.adapters.DashboardAdapter
import com.example.villageconnect.landowner.models.DashboardItem
import com.example.villageconnect.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.Calendar

class LandownerDashboard : Fragment(R.layout.fragment_landowner_dashboard) {

    private lateinit var tvGreeting: TextView
    private lateinit var tvLandownerName: TextView
    private lateinit var tvVillage: TextView
    private lateinit var tvFarmerCount: TextView
    private lateinit var tvServiceCount: TextView
    private lateinit var tvOrderCount: TextView
    private lateinit var rvDashboardCards: RecyclerView

    private lateinit var imgWeatherAnimation: ImageView
    private lateinit var tvWeatherTemperature: TextView
    private lateinit var tvWeatherStatus: TextView
    private lateinit var tvWeatherMeta: TextView
    private lateinit var tvWeatherAdvice: TextView

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

        imgWeatherAnimation = view.findViewById(R.id.imgWeatherAnimation)
        tvWeatherTemperature = view.findViewById(R.id.tvWeatherTemperature)
        tvWeatherStatus = view.findViewById(R.id.tvWeatherStatus)
        tvWeatherMeta = view.findViewById(R.id.tvWeatherMeta)
        tvWeatherAdvice = view.findViewById(R.id.tvWeatherAdvice)

        landownerId = SessionManager(requireContext()).getUserId()

        loadLandownerInfo()
        loadCounts()
        setupDashboardCards()
        loadWeatherInfo()
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
                tvVillage.text = getString(
                    R.string.village_label,
                    villageName ?: getString(R.string.not_updated)
                )
            }
        }
    }

    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
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

    private fun loadWeatherInfo() {
        val apiKey = BuildConfig.OPEN_WEATHER_API_KEY

        // Dhaka/Bhatara area fixed location
        val lat = 23.8103
        val lon = 90.4125

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val current = WeatherRetrofitClient.api.getCurrentWeather(
                    lat = lat,
                    lon = lon,
                    apiKey = apiKey
                )

                val forecast = WeatherRetrofitClient.api.getForecastWeather(
                    lat = lat,
                    lon = lon,
                    apiKey = apiKey
                )

                val temp = current.main.temp
                val humidity = current.main.humidity
                val condition = current.weather.firstOrNull()?.main ?: "Clear"

                val rainChance = forecast.list
                    .take(4)
                    .maxOfOrNull { it.pop ?: 0.0 } ?: 0.0

                val rainPercent = (rainChance * 100).toInt()

                updateWeatherCard(
                    temperature = temp,
                    humidity = humidity,
                    rainPercent = rainPercent,
                    condition = condition
                )

            } catch (e: Exception) {
                Log.e("WeatherError", "Failed to load weather: ${e.message}")
                showWeatherFallback()
            }
        }
    }

    private fun updateWeatherCard(
        temperature: Double,
        humidity: Int,
        rainPercent: Int,
        condition: String
    ) {
        val tempInt = temperature.toInt()

        tvWeatherTemperature.text = getString(R.string.weather_temp, tempInt)
        tvWeatherMeta.text = getString(R.string.weather_meta, rainPercent, humidity)

        when {
            rainPercent >= 60 || condition.contains("Rain", ignoreCase = true) -> {
                tvWeatherStatus.text = getString(R.string.weather_status_rain)
                tvWeatherAdvice.text = getString(R.string.weather_advice_rain)
                imgWeatherAnimation.setImageResource(R.drawable.img_weather_rain)
            }

            tempInt >= 38 -> {
                tvWeatherStatus.text = getString(R.string.weather_status_heatwave)
                tvWeatherAdvice.text = getString(R.string.weather_advice_heatwave)
                imgWeatherAnimation.setImageResource(R.drawable.img_weather_heatwave)
            }

            tempInt <= 12 -> {
                tvWeatherStatus.text = getString(R.string.weather_status_coldwave)
                tvWeatherAdvice.text = getString(R.string.weather_advice_coldwave)
                imgWeatherAnimation.setImageResource(R.drawable.img_weather_coldwave)
            }

            else -> {
                tvWeatherStatus.text = getString(R.string.weather_status_good)
                tvWeatherAdvice.text = getString(R.string.weather_advice_good)
                imgWeatherAnimation.setImageResource(R.drawable.img_weather_sunny)
            }
        }
    }

    private fun showWeatherFallback() {
        tvWeatherTemperature.text = getString(R.string.weather_temp_fallback)
        tvWeatherStatus.text = getString(R.string.weather_status_unavailable)
        tvWeatherMeta.text = getString(R.string.weather_meta_error)
        tvWeatherAdvice.text = getString(R.string.weather_advice_error)
        imgWeatherAnimation.setImageResource(R.drawable.img_weather_sunny)
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
                "Find Farmers" -> activity.loadFragment(FarmerList())
                "Book Agri Service" -> activity.loadFragment(MerchantList())
                "Agri Market" -> activity.loadFragment(InventoryList())
                "My Activity" -> activity.loadFragment(History())
            }
        }
    }
}