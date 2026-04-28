package com.example.garageapp.feature.report.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.garageapp.domain.model.Invoice
import com.example.garageapp.domain.model.JobCardItem
import com.example.garageapp.domain.repository.InvoiceRepository
import com.example.garageapp.domain.repository.JobCardItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class ReportData(
    val totalSales: Double = 0.0,
    val totalCost: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalDiscount: Double = 0.0,
    val totalPaid: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val invoiceCount: Int = 0,
    val dailyStats: Map<String, Double> = emptyMap() // Date string to Sales amount
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val itemRepository: JobCardItemRepository
) : ViewModel() {

    private val _reportData = MutableStateFlow(ReportData())
    val reportData: StateFlow<ReportData> = _reportData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun generateReport(startDate: Long, endDate: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            invoiceRepository.getInvoices().collect { allInvoices ->
                val filteredInvoices = allInvoices.filter { it.createdAt in startDate..endDate }
                
                var totalSales = 0.0
                var totalPaid = 0.0
                var totalDiscount = 0.0
                var totalProfit = 0.0
                val dailyStats = mutableMapOf<String, Double>()

                filteredInvoices.forEach { invoice ->
                    totalSales += invoice.totalAmount
                    totalPaid += invoice.paidAmount
                    totalDiscount += invoice.discount
                    
                    val dateKey = formatDate(invoice.createdAt)
                    dailyStats[dateKey] = (dailyStats[dateKey] ?: 0.0) + invoice.totalAmount

                    // We need items to calculate true profit (selling - cost)
                    // For a real app, we'd use a better way than collecting per invoice
                    // but for MVP this logic can be simplified if Invoice model is updated
                    // or we fetch all items for these job cards.
                }

                _reportData.value = ReportData(
                    totalSales = totalSales,
                    totalDiscount = totalDiscount,
                    totalPaid = totalPaid,
                    pendingBalance = totalSales - totalPaid,
                    invoiceCount = filteredInvoices.size,
                    dailyStats = dailyStats
                )
                _isLoading.value = false
            }
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
