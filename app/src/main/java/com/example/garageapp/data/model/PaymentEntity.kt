package com.example.garageapp.data.model

enum class PaymentMethodEntity {
    CASH, BANK_TRANSFER, OTHER
}

data class PaymentEntity(
    val paymentId: String = "",
    val shopId: String = "",
    val invoiceId: String = "",
    val amount: Double = 0.0,
    val paymentMethod: PaymentMethodEntity = PaymentMethodEntity.CASH,
    val note: String = "",
    val createdAt: Long = 0L
)