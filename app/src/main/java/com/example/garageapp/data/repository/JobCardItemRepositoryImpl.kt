package com.example.garageapp.data.repository

import com.example.garageapp.data.mapper.toDomain
import com.example.garageapp.data.mapper.toEntity
import com.example.garageapp.domain.model.JobCardItem
import com.example.garageapp.domain.repository.JobCardItemRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class JobCardItemRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : JobCardItemRepository {
    private val jobCardItemsRef get() = firestore.collection("jobCardItems")

    override fun getItemsForJobCard(jobCardId: String): Flow<List<JobCardItem>> = callbackFlow {
        val listener = jobCardItemsRef
            .whereEqualTo("jobCardId", jobCardId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { 
                        it.toObject(com.example.garageapp.data.model.JobCardItemEntity::class.java)?.toDomain() 
                    }
                    trySend(items)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addJobCardItem(item: JobCardItem) {
        val entity = item.toEntity()
        jobCardItemsRef.document(entity.itemId).set(entity).await()
    }

    override suspend fun updateJobCardItem(item: JobCardItem) {
        jobCardItemsRef.document(item.itemId).set(item.toEntity()).await()
    }

    override suspend fun deleteJobCardItem(itemId: String) {
        jobCardItemsRef.document(itemId).delete().await()
    }
}
