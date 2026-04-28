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
        loadDailyStats(System.currentTimeMillis())
    }

    fun loadDailyStats(date: Long) {
        _selectedDate.value = date
        viewModelScope.launch {
            _isLoading.value = true
            // Recalculate for today to ensure up-to-date data in MVP
            reportRepository.calculateAndStoreDailyStats(date)
            reportRepository.getDailyStats(date).collect {
                _dailyStats.value = it
                _isLoading.value = false
            }
        }
    }

    fun loadStatsForPeriod(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            reportRepository.getStatsForPeriod(startDate, endDate).collect {
                _periodStats.value = it
                _isLoading.value = false
            }
        }
    }
}
