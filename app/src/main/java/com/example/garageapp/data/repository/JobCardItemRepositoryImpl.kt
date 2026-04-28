package com.example.garageapp.data.repository

import com.example.garageapp.domain.model.JobCardItem
import com.example.garageapp.domain.repository.JobCardItemRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class JobCardItemRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : JobCardItemRepository {
    override fun getItemsForJobCard(jobCardId: String): Flow<List<JobCardItem>> = flowOf(emptyList())
    override suspend fun addJobCardItem(item: JobCardItem) {}
    override suspend fun updateJobCardItem(item: JobCardItem) {}
    override suspend fun deleteJobCardItem(itemId: String) {}
}
