package com.example.villageconnect.landowner.models

data class FarmerItem(
    val id: Int,
    val name: String,
    val phone: String,
    val village: String?,
    val skills: String?,
    val experience: String?,
    val dailyWage: Double?,
    var hasPendingRequest: Boolean = false
)