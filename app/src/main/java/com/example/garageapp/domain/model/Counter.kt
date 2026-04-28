package com.example.garageapp.domain.model

data class Counter(
    val counterId: String = "",
    val shopId: String = "",
    val jobCardNextNumber: Int = 1,
    val invoiceNextNumber: Int = 1
)