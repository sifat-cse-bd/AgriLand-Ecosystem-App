package com.example.villageconnect.farmer.models

data class JobOfferItem(
    val requestId: Int,
    val landownerId: Int,
    val landownerName: String,
    val phone: String,
    val village: String,
    val upazila: String,
    val district: String,
    val workDate: String,
    val status: String
)