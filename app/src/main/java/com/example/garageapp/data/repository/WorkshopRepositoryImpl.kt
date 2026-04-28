package com.example.garageapp.data.repository

import com.example.garageapp.data.mapper.toDomain
import com.example.garageapp.data.mapper.toEntity
import com.example.garageapp.domain.model.WorkshopDetails
import com.example.garageapp.domain.repository.WorkshopRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class WorkshopRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : WorkshopRepository {
    private val workshopsRef get() = firestore.collection("workshops")

    override fun getWorkshopDetails(shopId: String): Flow<WorkshopDetails?> = callbackFlow {
        val listener = workshopsRef.document(shopId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val details = snapshot.toObject(com.example.garageapp.data.model.WorkshopDetailsEntity::class.java)?.toDomain()
                    trySend(details)
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updateWorkshopDetails(details: WorkshopDetails) {
        workshopsRef.document(details.shopId).set(details.toEntity()).await()
    }
}
