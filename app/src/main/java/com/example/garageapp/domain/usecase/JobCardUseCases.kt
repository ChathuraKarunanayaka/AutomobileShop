package com.example.garageapp.domain.usecase

import com.example.garageapp.core.common.Constants
import com.example.garageapp.domain.model.JobCard
import com.example.garageapp.domain.repository.JobCardRepository
import com.example.garageapp.domain.repository.CounterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetJobCardsUseCase @Inject constructor(
    private val repository: JobCardRepository
) {
    operator fun invoke(): Flow<List<JobCard>> = repository.getJobCards()
}

class GetJobCardByIdUseCase @Inject constructor(
    private val repository: JobCardRepository
) {
    suspend operator fun invoke(jobCardId: String): JobCard? = repository.getJobCardById(jobCardId)
}

class UpdateJobCardUseCase @Inject constructor(
    private val repository: JobCardRepository
) {
    suspend operator fun invoke(jobCard: JobCard) = repository.updateJobCard(jobCard)
}

class AddJobCardUseCase @Inject constructor(
    private val repository: JobCardRepository,
    private val counterRepository: CounterRepository
) {
    suspend operator fun invoke(jobCard: JobCard) {
        val shopId = Constants.SHOP_ID
        val autoNumber = counterRepository.getNextJobCardNumber(shopId)
        val jobCardWithNumber = jobCard.copy(jobCardNumber = autoNumber, shopId = shopId)
        repository.addJobCard(jobCardWithNumber)
    }
}
