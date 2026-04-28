package com.example.garageapp.domain.usecase

import com.example.garageapp.domain.model.Invoice
import com.example.garageapp.domain.model.JobCard
import com.example.garageapp.domain.model.JobCardItem
import com.example.garageapp.domain.model.PaymentStatus
import com.example.garageapp.domain.repository.CounterRepository
import com.example.garageapp.domain.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GetInvoicesUseCase @Inject constructor(
    private val repository: InvoiceRepository
) {
    operator fun invoke(): Flow<List<Invoice>> = repository.getInvoices()
}

class GetInvoiceByIdUseCase @Inject constructor(
    private val repository: InvoiceRepository
) {
    suspend operator fun invoke(invoiceId: String): Invoice? = repository.getInvoiceById(invoiceId)
}

class CreateInvoiceUseCase @Inject constructor(
    private val repository: InvoiceRepository,
    private val counterRepository: CounterRepository
) {
    suspend operator fun invoke(
        jobCard: JobCard,
        items: List<JobCardItem>,
        discount: Double = 0.0,
        paidAmount: Double = 0.0
    ): Invoice {
        val shopId = jobCard.shopId.ifEmpty { "default" }
        val counter = counterRepository.getCounter(shopId)
        val nextNum = (counter?.invoiceNextNumber ?: 1)
        
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val vehicleSuffix = if (jobCard.vehicleNumber.length >= 4) {
            jobCard.vehicleNumber.takeLast(4)
        } else {
            jobCard.vehicleNumber
        }
        val invoiceNumber = "INV-$vehicleSuffix-$dateStr-${String.format("%03d", nextNum)}"
        
        val subtotal = items.sumOf { it.totalSellingPrice }
        val totalCost = items.sumOf { it.totalCost }
        val itemProfitSum = items.sumOf { it.profit }
        val totalAmount = subtotal - discount
        val totalProfit = itemProfitSum - discount
        
        val balanceAmount = totalAmount - paidAmount
        val paymentStatus = when {
            paidAmount <= 0 -> PaymentStatus.UNPAID
            paidAmount < totalAmount -> PaymentStatus.PARTIALLY_PAID
            else -> PaymentStatus.PAID
        }

        val invoice = Invoice(
            invoiceId = java.util.UUID.randomUUID().toString(),
            shopId = shopId,
            invoiceNumber = invoiceNumber,
            jobCardId = jobCard.jobCardId,
            customerId = jobCard.customerId,
            vehicleId = jobCard.vehicleId,
            customerName = jobCard.customerName,
            customerPhone = jobCard.customerPhone,
            vehicleNumber = jobCard.vehicleNumber,
            subtotal = subtotal,
            totalCost = totalCost,
            totalProfit = totalProfit,
            discount = discount,
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            balanceAmount = balanceAmount,
            paymentStatus = paymentStatus,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        repository.addInvoice(invoice)
        
        // Update counter
        counterRepository.updateCounter(
            (counter ?: com.example.garageapp.domain.model.Counter(shopId = shopId)).copy(
                invoiceNextNumber = nextNum + 1
            )
        )
        
        return invoice
    }
}
