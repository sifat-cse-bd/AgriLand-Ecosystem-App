package com.example.villageconnect.landowner.models

data class InventoryItem(
    val id: Int,
    val merchantId: Int,
    val itemName: String,
    val category: String?,
    val price: Double,
    val stockQty: Int
)