package com.example.garageapp.data.repository

import com.example.garageapp.domain.model.Payment
import com.example.garageapp.domain.repository.PaymentRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PaymentRepository {
    override fun getPaymentsForInvoice(invoiceId: String): Flow<List<Payment>> = flowOf(emptyList())
    override suspend fun addPayment(payment: Payment) {}
}
