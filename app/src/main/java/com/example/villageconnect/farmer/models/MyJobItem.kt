package com.example.villageconnect.farmer.models

data class MyJobItem(
    val hireRequestId: Int,
    val hireWorkId: Int?,
    val landownerName: String,
    val phone: String,
    val village: String,
    val workDate: String,
    val requestStatus: String,
    val workStatus: String
)