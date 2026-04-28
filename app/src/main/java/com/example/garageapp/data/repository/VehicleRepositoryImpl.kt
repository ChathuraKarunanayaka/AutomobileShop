package com.example.garageapp.data.repository

import com.example.garageapp.data.mapper.toDomain
import com.example.garageapp.data.mapper.toEntity
import com.example.garageapp.domain.model.Vehicle
import com.example.garageapp.domain.repository.VehicleRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class VehicleRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : VehicleRepository {
    private val vehiclesRef get() = firestore.collection("vehicles")

    override fun getVehicles(): Flow<List<Vehicle>> = callbackFlow {
        val listener = vehiclesRef.orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    val vehicles = snapshot.documents.mapNotNull { 
                        it.toObject(com.example.garageapp.data.model.VehicleEntity::class.java)?.toDomain() 
                    }
                    trySend(vehicles)
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getVehiclesByCustomer(customerId: String): Flow<List<Vehicle>> = callbackFlow {
        val listener = vehiclesRef.whereEqualTo("customerId", customerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    val vehicles = snapshot.documents.mapNotNull { 
                        it.toObject(com.example.garageapp.data.model.VehicleEntity::class.java)?.toDomain() 
                    }
                    trySend(vehicles)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getVehicleById(vehicleId: String): Vehicle? {
        return vehiclesRef.document(vehicleId).get().await()
            .toObject(com.example.garageapp.data.model.VehicleEntity::class.java)?.toDomain()
    }

    override suspend fun addVehicle(vehicle: Vehicle) {
        val entity = vehicle.toEntity()
        // We use set().await() to ensure it actually hits the server or fails
        vehiclesRef.document(entity.vehicleId).set(entity).await()
    }

    override suspend fun updateVehicle(vehicle: Vehicle) {
        vehiclesRef.document(vehicle.vehicleId).set(vehicle.toEntity()).await()
    }

    override suspend fun searchVehicles(query: String): List<Vehicle> {
        val snapshot = vehiclesRef
            .whereGreaterThanOrEqualTo("vehicleNumber", query.uppercase())
            .whereLessThanOrEqualTo("vehicleNumber", query.uppercase() + "\uf8ff")
            .get().await()
        return snapshot.documents.mapNotNull { 
            it.toObject(com.example.garageapp.data.model.VehicleEntity::class.java)?.toDomain() 
        }
    }
}
