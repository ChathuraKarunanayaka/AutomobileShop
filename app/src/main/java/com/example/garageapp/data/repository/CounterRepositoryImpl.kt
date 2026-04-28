package com.example.garageapp.data.repository

import com.example.garageapp.domain.model.Counter
import com.example.garageapp.domain.repository.CounterRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class CounterRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CounterRepository {
    override suspend fun getCounter(shopId: String): Counter? = null
    override suspend fun updateCounter(counter: Counter) {}
}
