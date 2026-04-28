package com.example.garageapp.domain.model

data class DailyStats(
    val date: Long = 0L,
    val totalSales: Double = 0.0,
    val totalCost: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalPaid: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val invoiceCount: Int = 0,
    val completedJobCards: Int = 0
)

data class DashboardStats(
    val openJobs: Int = 0,
    val completedJobs: Int = 0,
    val todaysIncome: Double = 0.0,
    val pendingPayments: Double = 0.0,
    val todaysProfit: Double = 0.0
)
