package com.example.villageconnect.farmer.models

data class JobOfferItem(
    val requestId: Int,
    val landownerId: Int,
    val landownerName: String,
    val phone: String,
    val district: String,
    val workDate: String,
    val status: String
)