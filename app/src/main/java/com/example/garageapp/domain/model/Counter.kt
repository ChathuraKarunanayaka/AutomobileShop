package com.example.garageapp.domain.model

data class Counter(
    val counterId: String = "",
    val shopId: String = "",
    val jobCardNextNumber: Long = 1L,
    val invoiceNextNumber: Long = 1L
)
