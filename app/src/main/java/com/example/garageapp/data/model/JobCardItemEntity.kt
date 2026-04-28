package com.example.garageapp.data.model

enum class JobCardItemTypeEntity {
    LABOUR, SPARE_PART, OUTSIDE_PURCHASE, SERVICE_CHARGE, OTHER
}

data class JobCardItemEntity(
    val itemId: String = "",
    val shopId: String = "",
    val jobCardId: String = "",
    val description: String = "",
    val itemType: JobCardItemTypeEntity = JobCardItemTypeEntity.LABOUR,
    val quantity: Int = 1,
    val costPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val totalCost: Double = 0.0,
    val totalSellingPrice: Double = 0.0,
    val profit: Double = 0.0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)