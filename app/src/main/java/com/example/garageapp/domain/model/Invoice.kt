package com.example.garageapp.domain.model

enum class PaymentStatus {
    UNPAID, PARTIALLY_PAID, PAID
}

data class Invoice(
    val invoiceId: String = "",
    val shopId: String = "",
    val invoiceNumber: String = "",
    val jobCardId: String = "",
    val customerId: String = "",
    val vehicleId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val vehicleNumber: String = "",
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val balanceAmount: Double = 0.0,
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)