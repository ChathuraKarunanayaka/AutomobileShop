package com.example.garageapp.data.repository

import com.example.garageapp.core.common.Constants
import com.example.garageapp.data.mapper.toDomain
import com.example.garageapp.data.model.InvoiceEntity
import com.example.garageapp.data.model.JobCardItemEntity
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

    private val shopId = Constants.SHOP_ID

    private fun getDailyStatsCollection() = 
        firestore.collection("shops").document(shopId).collection("reports_daily")

    override fun getDailyStats(date: Long): Flow<DailyStats?> = callbackFlow {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val normalizedDate = calendar.timeInMillis
        
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(normalizedDate))
        
        val listener = getDailyStatsCollection().document(dateStr)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val stats = snapshot.toObject(DailyStats::class.java)
                        trySend(stats)
                    } catch (e: Exception) {
                        trySend(DailyStats(date = normalizedDate))
                    }
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getStatsForPeriod(startDate: Long, endDate: Long): Flow<List<DailyStats>> = callbackFlow {
        val listener = getDailyStatsCollection()
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    try {
                        val stats = snapshot.toObjects(DailyStats::class.java).sortedBy { it.date }
                        trySend(stats)
                    } catch (e: Exception) {
                        trySend(emptyList())
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun calculateAndStoreDailyStats(date: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        try {
            // Get all invoices for this shop on this date
            val invoices = firestore.collection("invoices")
                .whereGreaterThanOrEqualTo("createdAt", startOfDay)
                .whereLessThanOrEqualTo("createdAt", endOfDay)
                .get().await().toObjects(InvoiceEntity::class.java)
                .filter { it.shopId == shopId } // Filter locally

            var totalSales = 0.0
            var totalPaid = 0.0
            var totalBalance = 0.0
            var totalProfit = 0.0
            var totalCost = 0.0
            var laborCharges = 0.0
            var sparePartsCost = 0.0
            var outsidePurchases = 0.0
            
            for (invoice in invoices) {
                totalSales += invoice.totalAmount
                totalPaid += invoice.paidAmount
                totalBalance += invoice.balanceAmount
                
                // Fetch all items for the job card associated with this invoice
                val items = firestore.collection("jobCardItems")
                    .whereEqualTo("jobCardId", invoice.jobCardId)
                    .get().await().toObjects(JobCardItemEntity::class.java)
                    .mapNotNull { entity ->
                        try { entity.toDomain() } catch (e: Exception) { null }
                    }
                
                items.forEach { item ->
                    when (item.itemType) {
                        JobCardItemType.LABOUR -> laborCharges += item.totalSellingPrice
                        JobCardItemType.SPARE_PART -> sparePartsCost += item.totalSellingPrice
                        JobCardItemType.OUTSIDE_PURCHASE -> outsidePurchases += item.totalSellingPrice
                        else -> {}
                    }
                    totalCost += item.totalCost
                    totalProfit += item.profit
                }
                // Important: Subtract discount from invoice-level profit
                totalProfit -= invoice.discount
            }

            val dailyStats = DailyStats(
                date = startOfDay,
                totalSales = totalSales,
                totalCost = totalCost,
                totalProfit = totalProfit,
                totalPaid = totalPaid,
                pendingBalance = totalBalance,
                invoiceCount = invoices.size.toLong(),
                completedJobCards = invoices.size.toLong(),
                laborCharges = laborCharges,
                sparePartsCost = sparePartsCost,
                outsidePurchases = outsidePurchases
            )

            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(date))
            getDailyStatsCollection().document(dateStr).set(dailyStats).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
