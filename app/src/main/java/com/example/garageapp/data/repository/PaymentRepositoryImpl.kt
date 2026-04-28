package com.example.garageapp.data.repository

import com.example.garageapp.data.mapper.toDomain
import com.example.garageapp.data.mapper.toEntity
import com.example.garageapp.domain.model.Payment
import com.example.garageapp.domain.repository.PaymentRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PaymentRepository {
    private val paymentsRef get() = firestore.collection("payments")

    override fun getPaymentsForInvoice(invoiceId: String): Flow<List<Payment>> = callbackFlow {
        val listener = paymentsRef
            .whereEqualTo("invoiceId", invoiceId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val payments = snapshot.documents.mapNotNull { 
                        it.toObject(com.example.garageapp.data.model.PaymentEntity::class.java)?.toDomain() 
                    }
                    trySend(payments)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addPayment(payment: Payment) {
        paymentsRef.document(payment.paymentId).set(payment.toEntity()).await()
    }
}
