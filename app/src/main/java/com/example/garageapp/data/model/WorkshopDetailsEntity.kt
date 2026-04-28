package com.example.garageapp.data.model

data class WorkshopDetailsEntity(
    val shopId: String = "default",
    val name: String = "",
    val address: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val logoUrl: String? = null,
    val footerNote: String = "Thank you for your business!",
    val updatedAt: Long = 0L
)
