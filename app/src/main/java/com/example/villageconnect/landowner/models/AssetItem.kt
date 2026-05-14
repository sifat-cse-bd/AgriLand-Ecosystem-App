package com.example.villageconnect.landowner.models

data class AssetItem(
    val id: Int,
    val merchantId: Int,
    val assetType: String,
    val status: String,
    val totalCapacity: Double?
)