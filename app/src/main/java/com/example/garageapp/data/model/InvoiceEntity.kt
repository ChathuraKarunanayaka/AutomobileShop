package com.example.garageapp.data.model

enum class PaymentStatusEntity {
    UNPAID, PARTIALLY_PAID, PAID
}

data class InvoiceEntity(
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
    val paymentStatus: PaymentStatusEntity = PaymentStatusEntity.UNPAID,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)