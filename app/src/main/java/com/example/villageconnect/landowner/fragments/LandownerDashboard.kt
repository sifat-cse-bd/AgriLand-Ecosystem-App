package com.example.villageconnect.landowner.fragments

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.villageconnect.BuildConfig
import com.example.villageconnect.R
import com.example.villageconnect.data.DBHelper
import com.example.villageconnect.data.DataAccess
import com.example.villageconnect.data.weather.WeatherRetrofitClient
import com.example.villageconnect.landowner.LandownerMainActivity
import com.example.villageconnect.landowner.adapters.DashboardAdapter
import com.example.villageconnect.landowner.models.DashboardItem
import com.example.villageconnect.utils.SessionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import java.util.Calendar
import android.Manifest
import android.location.Geocoder
import java.util.Locale

class LandownerDashboard : Fragment(R.layout.fragment_landowner_dashboard) {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
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
    private lateinit var tvWeatherUpdateStatus: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tvCurrentLocation: TextView

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            fetchCurrentLocationAndLoadWeather(isSwipeRefresh = false)
        } else {
            loadWeatherInfo(23.8103, 90.4125, isSwipeRefresh = false)
        }
    }


    private var landownerId = -1
    private var villageName: String? = null
    private var lastWeatherUpdateTime:Long = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        swipeRefreshLayout=view.findViewById(R.id.swipeRefreshLayout)
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
        tvWeatherUpdateStatus = view.findViewById(R.id.tvWeatherUpdateStatus)
        tvCurrentLocation=view.findViewById(R.id.tvCurrentLocation)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())


        landownerId = SessionManager(requireContext()).getUserId()

        onSwipe()
        loadLandownerInfo()
        loadCounts()
        setupDashboardCards()
        checkLocationPermissions()
    }

    private fun checkLocationPermissions() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                fetchCurrentLocationAndLoadWeather(isSwipeRefresh = false)
            }
            else -> {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocationAndLoadWeather(isSwipeRefresh: Boolean) {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    loadWeatherInfo(location.latitude, location.longitude, isSwipeRefresh)
                } else {
                    //set location dhaka if location fatching miss
                    loadWeatherInfo(23.8103, 90.4125, isSwipeRefresh)
                }
            }
            .addOnFailureListener {
                loadWeatherInfo(23.8103, 90.4125, isSwipeRefresh)
            }
    }

    private fun getPlaceName(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]

                val area = address.locality ?: address.subAdminArea ?: address.adminArea
                val country = address.countryName

                if (area != null) "$area, $country" else country ?: "Unknown Location"
            } else {
                "Unknown Location"
            }
        } catch (e: Exception) {
            Log.e("GeocoderError", "Failed to get place name: ${e.message}")
            "Unknown Location"
        }
    }

    private fun loadWeatherInfo(lat: Double, lon: Double, isSwipeRefresh: Boolean = false) {
        val apiKey = BuildConfig.OPEN_WEATHER_API_KEY

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val placeName = getPlaceName(lat, lon)
                tvCurrentLocation.text=placeName


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

                lastWeatherUpdateTime = System.currentTimeMillis()
                if(isSwipeRefresh)
                {
                    tvWeatherUpdateStatus.text = "✓ Succesfully updated."
                    viewLifecycleOwner.lifecycleScope.launch {
                        kotlinx.coroutines.delay(2000)
                        tvWeatherUpdateStatus.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                Log.e("WeatherError", "Failed to load weather: ${e.message}")
                showWeatherFallback()
                if(isSwipeRefresh)
                {
                    tvWeatherUpdateStatus.text = "✕ Failed to update."
                    viewLifecycleOwner.lifecycleScope.launch {
                        kotlinx.coroutines.delay(2000)
                        tvWeatherUpdateStatus.visibility = View.GONE
                    }
                }
            }
            finally {
                swipeRefreshLayout.isRefreshing = false
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

    @SuppressLint("ClickableViewAccessibility")
    private fun onSwipe() {
        var startY = 0f
        swipeRefreshLayout.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    val movedY = event.y - startY
                    if (movedY > 30f) {
                        tvWeatherUpdateStatus.visibility = View.VISIBLE
                        tvWeatherUpdateStatus.text = "Updating..."
                    }
                }
            }
            false
        }

        swipeRefreshLayout.setOnRefreshListener {
            val currentTime = System.currentTimeMillis()
            val timeDifference = currentTime - lastWeatherUpdateTime

            tvWeatherUpdateStatus.visibility = View.VISIBLE

            if (timeDifference < 5000) {
                swipeRefreshLayout.isRefreshing = false
                tvWeatherUpdateStatus.text = "Updated a few moments ago."

                viewLifecycleOwner.lifecycleScope.launch {
                    kotlinx.coroutines.delay(1500)
                    tvWeatherUpdateStatus.visibility = View.GONE
                }
            } else {
                tvWeatherUpdateStatus.text = "Updating..."
                loadLandownerInfo()
                loadCounts()
                setupDashboardCards()
                fetchCurrentLocationAndLoadWeather(isSwipeRefresh = true)
            }
        }
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
            SELECT COUNT(*) FROM ${DBHelper.TABLE_HIRE_REQUESTS}
            WHERE ${DBHelper.COL_LANDOWNER_ID} = ? 
              AND ${DBHelper.COL_REQUEST_STATUS} = '${DBHelper.STATUS_PENDING}'
              AND (${DBHelper.COL_EXPIRES_AT} > CURRENT_TIMESTAMP OR ${DBHelper.COL_EXPIRES_AT} IS NULL)
            """,
            arrayOf(landownerId.toString())
        ).toString()

        tvServiceCount.text = getCount(
            """
            SELECT COUNT(*) FROM ${DBHelper.TABLE_HIRE_WORK} hw
            JOIN ${DBHelper.TABLE_HIRE_REQUESTS} hr ON hw.${DBHelper.COL_HIRE_REQUEST_ID} = hr.${DBHelper.COL_ID}
            WHERE hr.${DBHelper.COL_LANDOWNER_ID} = ? 
              AND hw.${DBHelper.COL_WORK_STATUS} = '${DBHelper.STATUS_ON_WORK}'
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
}