package com.example.garageapp.domain.model

data class User(
    val userId: String = "",
    val shopId: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val createdAt: Long = 0L
)