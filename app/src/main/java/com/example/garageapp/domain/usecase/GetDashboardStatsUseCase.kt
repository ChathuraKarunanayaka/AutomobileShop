package com.example.garageapp.domain.usecase

import com.example.garageapp.domain.model.JobCard
import com.example.garageapp.domain.model.Invoice
import com.example.garageapp.domain.repository.JobCardRepository
import com.example.garageapp.domain.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class DashboardStats(
    val openJobs: Int = 0,
    val completedJobs: Int = 0,
    val todaysIncome: Double = 0.0,
    val pendingPayments: Double = 0.0
)

class GetDashboardStatsUseCase @Inject constructor(
    private val jobCardRepository: JobCardRepository,
    private val invoiceRepository: InvoiceRepository
) {
    operator fun invoke(): Flow<DashboardStats> =
        combine(
            jobCardRepository.getJobCards(),
            invoiceRepository.getInvoices()
        ) { jobCards, invoices ->
            val openJobs = jobCards.count { it.status.name == "OPEN" || it.status.name == "IN_PROGRESS" }
            val completedJobs = jobCards.count { it.status.name == "COMPLETED" }
            val today = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
            val todaysInvoices = invoices.filter {
                (it.createdAt / (1000 * 60 * 60 * 24)) == today
            }
            val todaysIncome = todaysInvoices.sumOf { it.paidAmount }
            val pendingPayments = invoices.filter { it.balanceAmount > 0 }.sumOf { it.balanceAmount }
            DashboardStats(
                openJobs = openJobs,
                completedJobs = completedJobs,
                todaysIncome = todaysIncome,
                pendingPayments = pendingPayments
            )
        }
}
