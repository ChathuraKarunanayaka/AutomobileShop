package com.example.garageapp.domain.repository

import com.example.garageapp.domain.model.*
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun getCustomers(): Flow<List<Customer>>
    suspend fun getCustomerById(customerId: String): Customer?
    suspend fun addCustomer(customer: Customer)
    suspend fun updateCustomer(customer: Customer)
    suspend fun searchCustomers(query: String): List<Customer>
}

interface VehicleRepository {
    fun getVehicles(): Flow<List<Vehicle>>
    fun getVehiclesByCustomer(customerId: String): Flow<List<Vehicle>>
    suspend fun getVehicleById(vehicleId: String): Vehicle?
    suspend fun addVehicle(vehicle: Vehicle)
    suspend fun updateVehicle(vehicle: Vehicle)
    suspend fun searchVehicles(query: String): List<Vehicle>
}

interface JobCardRepository {
    fun getJobCards(): Flow<List<JobCard>>
    suspend fun getJobCardById(jobCardId: String): JobCard?
    suspend fun addJobCard(jobCard: JobCard)
    suspend fun updateJobCard(jobCard: JobCard)
    suspend fun searchJobCards(query: String): List<JobCard>
}

interface JobCardItemRepository {
    fun getItemsForJobCard(jobCardId: String): Flow<List<JobCardItem>>
    suspend fun addJobCardItem(item: JobCardItem)
    suspend fun updateJobCardItem(item: JobCardItem)
    suspend fun deleteJobCardItem(itemId: String)
}

interface InvoiceRepository {
    fun getInvoices(): Flow<List<Invoice>>
    suspend fun getInvoiceById(invoiceId: String): Invoice?
    suspend fun addInvoice(invoice: Invoice)
    suspend fun updateInvoice(invoice: Invoice)
    suspend fun searchInvoices(query: String): List<Invoice>
}

interface PaymentRepository {
    fun getPaymentsForInvoice(invoiceId: String): Flow<List<Payment>>
    suspend fun addPayment(payment: Payment)
}

interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signOut()
}

interface CounterRepository {
    suspend fun getCounter(shopId: String): Counter?
    suspend fun updateCounter(counter: Counter)
}
