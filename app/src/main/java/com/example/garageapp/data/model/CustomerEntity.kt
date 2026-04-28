package com.example.garageapp.data.model

import com.example.garageapp.domain.model.*

data class CustomerEntity(
    val customerId: String = "",
    val shopId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val searchKeywords: List<String> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)