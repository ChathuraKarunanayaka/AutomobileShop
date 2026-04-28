package com.example.garageapp.domain.repository

import com.example.garageapp.domain.model.DailyStats
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun getDailyStats(date: Long): Flow<DailyStats?>
    fun getStatsForPeriod(startDate: Long, endDate: Long): Flow<List<DailyStats>>
    suspend fun calculateAndStoreDailyStats(date: Long)
}
