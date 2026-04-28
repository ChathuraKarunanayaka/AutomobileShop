package com.example.garageapp.data.repository

import com.example.garageapp.domain.model.Counter
import com.example.garageapp.domain.repository.CounterRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CounterRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CounterRepository {

    private fun getCounterRef(shopId: String) = 
        firestore.collection("shops").document(shopId).collection("counters").document("main")

    override suspend fun getCounter(shopId: String): Counter? {
        return getCounterRef(shopId).get().await().toObject(Counter::class.java)
    }

    override suspend fun updateCounter(counter: Counter) {
        // shopId is part of the counter object
        val shopId = "demo_shop" // In production, get from auth
        getCounterRef(shopId).set(counter).await()
    }

    suspend fun getNextJobCardNumber(shopId: String): String {
        return firestore.runTransaction { transaction ->
            val ref = getCounterRef(shopId)
            val snapshot = transaction.get(ref)
            val currentCount = snapshot.getLong("jobCardNextNumber") ?: 1L
            transaction.update(ref, "jobCardNextNumber", currentCount + 1)
            
            val dateStr = java.text.SimpleDateFormat("yyMMdd", java.util.Locale.getDefault()).format(java.util.Date())
            "JC-$dateStr-${String.format("%03d", currentCount)}"
        }.await()
    }

    suspend fun getNextInvoiceNumber(shopId: String, vehicleNumber: String): String {
        return firestore.runTransaction { transaction ->
            val ref = getCounterRef(shopId)
            val snapshot = transaction.get(ref)
            val currentCount = snapshot.getLong("invoiceNextNumber") ?: 1L
            transaction.update(ref, "invoiceNextNumber", currentCount + 1)
            
            val lastFour = if (vehicleNumber.length >= 4) vehicleNumber.takeLast(4) else vehicleNumber
            val dateStr = java.text.SimpleDateFormat("yyMMdd", java.util.Locale.getDefault()).format(java.util.Date())
            "INV-$lastFour-$dateStr-${String.format("%03d", currentCount)}"
        }.await()
    }
}
