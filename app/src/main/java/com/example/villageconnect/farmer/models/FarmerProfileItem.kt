package com.example.villageconnect.farmer.models


data class FarmerProfileItem(
    val userId: Int,
    val fullName: String,
    val phone: String,
    val district: String,
    val skills: String,
    val experience: String,
    val dailyWage: Double,
    val bio: String
)