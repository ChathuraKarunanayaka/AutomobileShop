package com.example.garageapp.data.repository

import com.example.garageapp.data.mapper.toDomain
import com.example.garageapp.data.mapper.toEntity
import com.example.garageapp.domain.model.Invoice
import com.example.garageapp.domain.repository.InvoiceRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InvoiceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : InvoiceRepository {
    private val invoicesRef get() = firestore.collection("invoices")

    override fun getInvoices(): Flow<List<Invoice>> = callbackFlow {
        val listener = invoicesRef
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val invoices = snapshot.documents.mapNotNull { 
                        it.toObject(com.example.garageapp.data.model.InvoiceEntity::class.java)?.toDomain() 
                    }
                    trySend(invoices)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getInvoiceById(invoiceId: String): Invoice? {
        return invoicesRef.document(invoiceId).get().await()
            .toObject(com.example.garageapp.data.model.InvoiceEntity::class.java)?.toDomain()
    }

    override suspend fun addInvoice(invoice: Invoice) {
        invoicesRef.document(invoice.invoiceId).set(invoice.toEntity()).await()
    }

    override suspend fun updateInvoice(invoice: Invoice) {
        invoicesRef.document(invoice.invoiceId).set(invoice.toEntity()).await()
    }

    override suspend fun searchInvoices(query: String): List<Invoice> {
        // Simplified search, Firestore doesn't support partial match well without third party
        // For MVP, we can fetch all or a subset and filter locally if needed, 
        // or just rely on the Flow and local filtering in ViewModel.
        val snapshot = invoicesRef.get().await()
        return snapshot.documents.mapNotNull { 
            it.toObject(com.example.garageapp.data.model.InvoiceEntity::class.java)?.toDomain() 
        }.filter { 
            it.invoiceNumber.contains(query, ignoreCase = true) || 
            it.customerName.contains(query, ignoreCase = true) ||
            it.vehicleNumber.contains(query, ignoreCase = true)
        }
    }
}
