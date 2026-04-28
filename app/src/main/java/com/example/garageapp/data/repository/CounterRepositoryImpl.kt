package com.example.garageapp.data.repository

import com.example.garageapp.domain.model.Counter
import com.example.garageapp.domain.repository.CounterRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class CounterRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CounterRepository {

    private fun getCounterRef(shopId: String) = 
        firestore.collection("shops").document(shopId).collection("counters").document("main")

    override suspend fun getCounter(shopId: String): Counter? {
        val id = if (shopId.isEmpty()) "demo_shop" else shopId
        return getCounterRef(id).get().await().toObject(Counter::class.java)
    }

    override suspend fun updateCounter(counter: Counter) {
        val id = if (counter.shopId.isEmpty()) "demo_shop" else counter.shopId
        getCounterRef(id).set(counter, SetOptions.merge()).await()
    }

    override suspend fun getNextJobCardNumber(shopId: String): String {
        val id = if (shopId.isEmpty()) "demo_shop" else shopId
        val dateStr = SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Date())
        
        return firestore.runTransaction { transaction ->
            val ref = getCounterRef(id)
            val snapshot = transaction.get(ref)
            
            // Get current count or default to 1
            val currentCount = snapshot.getLong("jobCardNextNumber") ?: 1L
            
            // Increment the counter in the DB
            transaction.set(ref, mapOf("jobCardNextNumber" to currentCount + 1), SetOptions.merge())
            
            // Return the formatted number (e.g., JC-231027-001)
            "JC-$dateStr-${String.format("%03d", currentCount)}"
        }.await()
    }

    override suspend fun getNextInvoiceNumber(shopId: String, vehicleNumber: String): String {
        val id = if (shopId.isEmpty()) "demo_shop" else shopId
        val dateStr = SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Date())
        val lastFour = if (vehicleNumber.length >= 4) vehicleNumber.takeLast(4) else vehicleNumber
        
        return firestore.runTransaction { transaction ->
            val ref = getCounterRef(id)
            val snapshot = transaction.get(ref)
            
            // Get current count or default to 1
            val currentCount = snapshot.getLong("invoiceNextNumber") ?: 1L
            
            // Increment the counter in the DB
            transaction.set(ref, mapOf("invoiceNextNumber" to currentCount + 1), SetOptions.merge())
            
            // Return the formatted number (e.g., INV-1234-231027-001)
            "INV-$lastFour-$dateStr-${String.format("%03d", currentCount)}"
        }.await()
    }
}
