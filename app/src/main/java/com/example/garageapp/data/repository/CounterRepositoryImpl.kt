package com.example.garageapp.data.repository

import com.example.garageapp.core.common.Constants
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

    private val shopId = Constants.SHOP_ID

    private fun getCounterRef() = 
        firestore.collection("shops").document(shopId).collection("counters").document("main")

    override suspend fun getCounter(shopId: String): Counter? {
        return getCounterRef().get().await().toObject(Counter::class.java)
    }

    override suspend fun updateCounter(counter: Counter) {
        getCounterRef().set(counter, SetOptions.merge()).await()
    }

    override suspend fun getNextJobCardNumber(shopId: String): String {
        val dateStr = SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Date())
        
        return firestore.runTransaction { transaction ->
            val ref = getCounterRef()
            val snapshot = transaction.get(ref)
            
            val currentCount = snapshot.getLong("jobCardNextNumber") ?: 1L
            transaction.set(ref, mapOf("jobCardNextNumber" to currentCount + 1), SetOptions.merge())
            
            "JC-$dateStr-${String.format("%03d", currentCount)}"
        }.await()
    }

    override suspend fun getNextInvoiceNumber(shopId: String, vehicleNumber: String): String {
        val dateStr = SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Date())
        val lastFour = if (vehicleNumber.length >= 4) vehicleNumber.takeLast(4) else vehicleNumber
        
        return firestore.runTransaction { transaction ->
            val ref = getCounterRef()
            val snapshot = transaction.get(ref)
            
            val currentCount = snapshot.getLong("invoiceNextNumber") ?: 1L
            transaction.set(ref, mapOf("invoiceNextNumber" to currentCount + 1), SetOptions.merge())
            
            "INV-$lastFour-$dateStr-${String.format("%03d", currentCount)}"
        }.await()
    }

    override suspend fun resetCounters(shopId: String) {
        TODO("Not yet implemented")
    }
}
