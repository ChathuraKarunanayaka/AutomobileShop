package com.example.garageapp.data.model

enum class JobCardStatusEntity {
    OPEN, IN_PROGRESS, READY_FOR_DELIVERY, COMPLETED, CANCELLED
}

data class JobCardEntity(
    val jobCardId: String = "",
    val shopId: String = "",
    val jobCardNumber: String = "",
    val customerId: String = "",
    val vehicleId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val vehicleNumber: String = "",
    val status: JobCardStatusEntity = JobCardStatusEntity.OPEN,
    val complaintDescription: String = "",
    val inspectionNotes: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val completedAt: Long? = null
)