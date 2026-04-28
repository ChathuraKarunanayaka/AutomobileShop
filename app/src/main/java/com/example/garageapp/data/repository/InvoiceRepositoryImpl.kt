package com.example.garageapp.data.repository

import com.example.garageapp.domain.model.Invoice
import com.example.garageapp.domain.repository.InvoiceRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class InvoiceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : InvoiceRepository {
    override fun getInvoices(): Flow<List<Invoice>> = flowOf(emptyList())
    override suspend fun getInvoiceById(invoiceId: String): Invoice? = null
    override suspend fun addInvoice(invoice: Invoice) {}
    override suspend fun updateInvoice(invoice: Invoice) {}
    override suspend fun searchInvoices(query: String): List<Invoice> = emptyList()
}
