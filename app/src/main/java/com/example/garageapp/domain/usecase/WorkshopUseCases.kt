package com.example.garageapp.domain.usecase

import com.example.garageapp.domain.model.WorkshopDetails
import com.example.garageapp.domain.repository.WorkshopRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWorkshopDetailsUseCase @Inject constructor(
    private val repository: WorkshopRepository
) {
    operator fun invoke(shopId: String = "default"): Flow<WorkshopDetails?> = 
        repository.getWorkshopDetails(shopId)
}

class UpdateWorkshopDetailsUseCase @Inject constructor(
    private val repository: WorkshopRepository
) {
    suspend operator fun invoke(details: WorkshopDetails) = 
        repository.updateWorkshopDetails(details)
}
