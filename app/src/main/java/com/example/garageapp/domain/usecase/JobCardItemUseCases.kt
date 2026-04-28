package com.example.garageapp.domain.usecase

import com.example.garageapp.domain.model.JobCardItem
import com.example.garageapp.domain.repository.JobCardItemRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetJobCardItemsUseCase @Inject constructor(
    private val repository: JobCardItemRepository
) {
    operator fun invoke(jobCardId: String): Flow<List<JobCardItem>> = 
        repository.getItemsForJobCard(jobCardId)
}

class AddJobCardItemUseCase @Inject constructor(
    private val repository: JobCardItemRepository
) {
    suspend operator fun invoke(item: JobCardItem) {
        val totalCost = item.quantity * item.costPrice
        val totalSellingPrice = item.quantity * item.sellingPrice
        val profit = totalSellingPrice - totalCost
        
        val updatedItem = item.copy(
            totalCost = totalCost,
            totalSellingPrice = totalSellingPrice,
            profit = profit,
            updatedAt = System.currentTimeMillis()
        )
        repository.addJobCardItem(updatedItem)
    }
}

class DeleteJobCardItemUseCase @Inject constructor(
    private val repository: JobCardItemRepository
) {
    suspend operator fun invoke(itemId: String) = repository.deleteJobCardItem(itemId)
}
