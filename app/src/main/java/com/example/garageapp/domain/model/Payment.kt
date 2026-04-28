package com.example.garageapp.domain.model

enum class PaymentMethod {
    CASH, BANK_TRANSFER, OTHER
}

data class Payment(
    val paymentId: String = "",
    val shopId: String = "",
    val invoiceId: String = "",
    val amount: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val note: String = "",
    val createdAt: Long = 0L
)