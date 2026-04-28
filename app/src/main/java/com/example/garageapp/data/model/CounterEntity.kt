package com.example.garageapp.data.model

data class CounterEntity(
    val counterId: String = "",
    val shopId: String = "",
    val jobCardNextNumber: Long = 1L,
    val invoiceNextNumber: Long = 1L
)
