package com.example.garageapp.data.model

data class CounterEntity(
    val counterId: String = "",
    val shopId: String = "",
    val jobCardNextNumber: Int = 1,
    val invoiceNextNumber: Int = 1
)