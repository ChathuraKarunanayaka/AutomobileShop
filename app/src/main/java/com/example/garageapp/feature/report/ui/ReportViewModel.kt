package com.example.garageapp.feature.report.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.garageapp.domain.model.DailyStats
import com.example.garageapp.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class PeriodSummary(
    val totalSales: Double = 0.0,
    val totalCost: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalPaid: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val invoiceCount: Long = 0,
    val laborCharges: Double = 0.0,
    val sparePartsCost: Double = 0.0,
    val outsidePurchases: Double = 0.0
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _startDate = MutableStateFlow<Long?>(null)
    val startDate: StateFlow<Long?> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<Long?>(null)
    val endDate: StateFlow<Long?> = _endDate.asStateFlow()

    private val _dailyStats = MutableStateFlow<DailyStats?>(null)
    val dailyStats: StateFlow<DailyStats?> = _dailyStats.asStateFlow()

    private val _periodStats = MutableStateFlow<List<DailyStats>>(emptyList())
    val periodStats: StateFlow<List<DailyStats>> = _periodStats.asStateFlow()

    private val _periodSummary = MutableStateFlow(PeriodSummary())
    val periodSummary: StateFlow<PeriodSummary> = _periodSummary.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        val now = System.currentTimeMillis()
        loadDailyStats(now)
        
        // Initial load for last 30 days
        val calendar = Calendar.getInstance()
        val end = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val start = calendar.timeInMillis
        _startDate.value = start
        _endDate.value = end
        
        loadStatsForPeriod(start, end)
    }

    fun loadDailyStats(date: Long) {
        _selectedDate.value = date
        viewModelScope.launch {
            _isLoading.value = true
            reportRepository.calculateAndStoreDailyStats(date)
            reportRepository.getDailyStats(date).take(1).collect {
                _dailyStats.value = it
                _isLoading.value = false
            }
        }
    }

    fun loadStatsForPeriod(start: Long, end: Long) {
        _startDate.value = start
        _endDate.value = end
        _isLoading.value = true
        
        viewModelScope.launch {
            // Trigger calculation for each day in range to ensure data exists
            // For a larger range, this might need a different strategy, but for MVP it works
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = start
            while (calendar.timeInMillis <= end) {
                reportRepository.calculateAndStoreDailyStats(calendar.timeInMillis)
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            reportRepository.getStatsForPeriod(start, end).collectLatest { stats ->
                _periodStats.value = stats
                
                // Calculate period summary
                _periodSummary.value = PeriodSummary(
                    totalSales = stats.sumOf { it.totalSales },
                    totalCost = stats.sumOf { it.totalCost },
                    totalProfit = stats.sumOf { it.totalProfit },
                    totalPaid = stats.sumOf { it.totalPaid },
                    pendingBalance = stats.sumOf { it.pendingBalance },
                    invoiceCount = stats.sumOf { it.invoiceCount },
                    laborCharges = stats.sumOf { it.laborCharges },
                    sparePartsCost = stats.sumOf { it.sparePartsCost },
                    outsidePurchases = stats.sumOf { it.outsidePurchases }
                )
                _isLoading.value = false
            }
        }
    }
}
