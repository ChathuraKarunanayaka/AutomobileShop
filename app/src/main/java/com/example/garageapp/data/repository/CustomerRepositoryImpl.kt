package com.example.garageapp.data.repository

import com.example.garageapp.data.mapper.toDomain
import com.example.garageapp.data.mapper.toEntity
import com.example.garageapp.domain.model.Customer
import com.example.garageapp.domain.repository.CustomerRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CustomerRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CustomerRepository {
    private val customersRef get() = firestore.collection("customers")

    override fun getCustomers(): Flow<List<Customer>> = callbackFlow {
        val listener = customersRef.orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val customers = snapshot.documents.mapNotNull { it.toObject(com.example.garageapp.data.model.CustomerEntity::class.java)?.toDomain() }
                    trySend(customers)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getCustomerById(customerId: String): Customer? {
        val doc = customersRef.document(customerId).get().await()
        return doc.toObject(com.example.garageapp.data.model.CustomerEntity::class.java)?.toDomain()
    }

    override suspend fun addCustomer(customer: Customer) {
        val entity = customer.toEntity().copy(
            searchKeywords = generateSearchKeywords(customer.name, customer.phoneNumber)
        )
        customersRef.document(entity.customerId).set(entity).await()
    }

    override suspend fun updateCustomer(customer: Customer) {
        val entity = customer.toEntity().copy(
            searchKeywords = generateSearchKeywords(customer.name, customer.phoneNumber)
        )
        customersRef.document(entity.customerId).set(entity).await()
    }

    override suspend fun searchCustomers(query: String): List<Customer> {
        if (query.isBlank()) return emptyList()
        val snapshot = customersRef
            .whereArrayContains("searchKeywords", query.lowercase())
            .limit(20)
            .get().await()
        return snapshot.documents.mapNotNull { it.toObject(com.example.garageapp.data.model.CustomerEntity::class.java)?.toDomain() }
    }

    private fun generateSearchKeywords(name: String, phone: String): List<String> {
        val keywords = mutableSetOf<String>()
        
        // Add name parts
        val nameParts = name.lowercase().split(" ")
        for (part in nameParts) {
            for (i in 1..part.length) {
                keywords.add(part.substring(0, i))
            }
        }
        
        // Add full name for exact matches
        keywords.add(name.lowercase())
        
        // Add phone number parts
        for (i in 1..phone.length) {
            keywords.add(phone.substring(0, i))
        }
        
        return keywords.toList()
    }
}
