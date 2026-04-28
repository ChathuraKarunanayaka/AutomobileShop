package com.example.garageapp.data.repository

import com.example.garageapp.data.mapper.toDomain
import com.example.garageapp.data.mapper.toEntity
import com.example.garageapp.domain.model.JobCard
import com.example.garageapp.domain.repository.JobCardRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class JobCardRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : JobCardRepository {
    private val jobCardsRef get() = firestore.collection("jobCards")

    override fun getJobCards(): Flow<List<JobCard>> = callbackFlow {
        val listener = jobCardsRef.orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val jobCards = snapshot.documents.mapNotNull { 
                        it.toObject(com.example.garageapp.data.model.JobCardEntity::class.java)?.toDomain() 
                    }
                    trySend(jobCards)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getJobCardById(jobCardId: String): JobCard? {
        return jobCardsRef.document(jobCardId).get().await()
            .toObject(com.example.garageapp.data.model.JobCardEntity::class.java)?.toDomain()
    }

    override suspend fun addJobCard(jobCard: JobCard) {
        val entity = jobCard.toEntity()
        jobCardsRef.document(entity.jobCardId).set(entity).await()
    }

    override suspend fun updateJobCard(jobCard: JobCard) {
        jobCardsRef.document(jobCard.jobCardId).set(jobCard.toEntity()).await()
    }

    override suspend fun searchJobCards(query: String): List<JobCard> {
        // Simple search by job card number or customer name snapshot
        val snapshot = jobCardsRef
            .whereGreaterThanOrEqualTo("jobCardNumber", query.uppercase())
            .whereLessThanOrEqualTo("jobCardNumber", query.uppercase() + "\uf8ff")
            .get().await()
        return snapshot.documents.mapNotNull { 
            it.toObject(com.example.garageapp.data.model.JobCardEntity::class.java)?.toDomain() 
        }
    }
}
