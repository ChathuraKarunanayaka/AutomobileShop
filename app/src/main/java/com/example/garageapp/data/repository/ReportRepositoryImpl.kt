package com.example.garageapp.data.repository

import com.example.garageapp.domain.model.*
import com.example.garageapp.domain.repository.ReportRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReportRepository {

    private fun getDailyStatsCollection(shopId: String) = 
        firestore.collection("shops").document(shopId).collection("reports_daily")

    override fun getDailyStats(date: Long): Flow<DailyStats?> = callbackFlow {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(date))
        // Assuming we have shopId from current user context, for MVP using "demo_shop"
        val shopId = "demo_shop" 
        
        val listener = getDailyStatsCollection(shopId).document(dateStr)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    trySend(it.toObject(DailyStats::class.java))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getStatsForPeriod(startDate: Long, endDate: Long): Flow<List<DailyStats>> = callbackFlow {
        val shopId = "demo_shop"
        val startStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(startDate))
        val endStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(endDate))
        
        val listener = getDailyStatsCollection(shopId)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    trySend(it.toObjects(DailyStats::class.java).sortedBy { it.date })
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun calculateAndStoreDailyStats(date: Long) {
        val shopId = "demo_shop"
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = calendar.timeInMillis

        // 1. Fetch all invoices for this day
        val invoices = firestore.collection("shops").document(shopId).collection("invoices")
            .whereGreaterThanOrEqualTo("createdAt", startOfDay)
            .whereLessThanOrEqualTo("createdAt", endOfDay)
            .get().await().toObjects(Invoice::class.java)

        // 2. Calculate totals
        var totalSales = 0.0
        var totalPaid = 0.0
        var totalBalance = 0.0
        
        invoices.forEach {
            totalSales += it.totalAmount
            totalPaid += it.paidAmount
            totalBalance += it.balanceAmount
        }

        // 3. Fetch JobCard items for profit calculation (simplified for MVP)
        // In a real app, you might want to pre-calculate profit when invoice is created
        var totalProfit = 0.0
        for (invoice in invoices) {
            val items = firestore.collection("shops").document(shopId).collection("jobCards")
                .document(invoice.jobCardId).collection("items")
                .get().await().toObjects(JobCardItem::class.java)
            
            val invoiceProfit = items.sumOf { it.profit } - invoice.discount
            totalProfit += invoiceProfit
        }

        val completedJobs = invoices.size // Simplified: one invoice per completed job

        val dailyStats = DailyStats(
            date = startOfDay,
            totalSales = totalSales,
            totalCost = totalSales - totalProfit,
            totalProfit = totalProfit,
            totalPaid = totalPaid,
            pendingBalance = totalBalance,
            invoiceCount = invoices.size,
            completedJobCards = completedJobs
        )

        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(date))
        getDailyStatsCollection(shopId).document(dateStr).set(dailyStats).await()
    }
}
