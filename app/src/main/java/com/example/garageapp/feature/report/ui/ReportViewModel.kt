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

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _dailyStats = MutableStateFlow<DailyStats?>(null)
    val dailyStats: StateFlow<DailyStats?> = _dailyStats.asStateFlow()

    private val _periodStats = MutableStateFlow<List<DailyStats>>(emptyList())
    val periodStats: StateFlow<List<DailyStats>> = _periodStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        val now = System.currentTimeMillis()
        loadDailyStats(now)
        
        // Automatically calculate stats for the last 7 days to populate the chart
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            for (i in 0..6) {
                reportRepository.calculateAndStoreDailyStats(calendar.timeInMillis)
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }
            
            // After calculating, load the period for display
            val end = System.currentTimeMillis()
            calendar.timeInMillis = end
            calendar.add(Calendar.DAY_OF_YEAR, -30)
            val start = calendar.timeInMillis
            loadStatsForPeriod(start, end)
        }
    }

    fun loadDailyStats(date: Long) {
        _selectedDate.value = date
        viewModelScope.launch {
            _isLoading.value = true
            reportRepository.calculateAndStoreDailyStats(date)
            reportRepository.getDailyStats(date).collect {
                _dailyStats.value = it
                _isLoading.value = false
            }
        }
    }

    fun loadStatsForPeriod(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            reportRepository.getStatsForPeriod(startDate, endDate).collect {
                _periodStats.value = it
            }
        }
    }
}
