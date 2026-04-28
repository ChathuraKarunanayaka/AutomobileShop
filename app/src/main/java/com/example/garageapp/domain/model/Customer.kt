package com.example.garageapp.domain.model

data class Customer(
    val customerId: String = "",
    val shopId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)