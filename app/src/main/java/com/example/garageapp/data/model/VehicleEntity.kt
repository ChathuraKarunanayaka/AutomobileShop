package com.example.garageapp.data.model

data class VehicleEntity(
    val vehicleId: String = "",
    val shopId: String = "",
    val customerId: String = "",
    val vehicleNumber: String = "",
    val model: String = "",
    val notes: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)