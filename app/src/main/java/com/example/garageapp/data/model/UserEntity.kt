package com.example.garageapp.data.model

data class UserEntity(
    val userId: String = "",
    val shopId: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val createdAt: Long = 0L
)