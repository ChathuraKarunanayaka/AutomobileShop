package com.example.garageapp.domain.usecase

import com.example.garageapp.domain.model.JobCard
import com.example.garageapp.domain.repository.JobCardRepository
import com.example.garageapp.domain.repository.CounterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GetJobCardsUseCase @Inject constructor(
    private val repository: JobCardRepository
) {
    operator fun invoke(): Flow<List<JobCard>> = repository.getJobCards()
}

class AddJobCardUseCase @Inject constructor(
    private val repository: JobCardRepository,
    private val counterRepository: CounterRepository
) {
    suspend operator fun invoke(jobCard: JobCard) {
        // Here we could implement the JC-date-001 logic
        // For simplicity in this step, we assume jobCardNumber is already set or handled
        repository.addJobCard(jobCard)
    }
}

class GenerateJobCardNumberUseCase @Inject constructor(
    private val counterRepository: CounterRepository
) {
    suspend operator fun invoke(shopId: String = "default"): String {
        val counter = counterRepository.getCounter(shopId)
        val nextNum = (counter?.jobCardNextNumber ?: 1)
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val formattedNum = String.format("%03d", nextNum)
        
        // Increment counter
        counterRepository.updateCounter(
            (counter ?: com.example.garageapp.domain.model.Counter(shopId = shopId)).copy(
                jobCardNextNumber = nextNum + 1
            )
        )
        
        return "JC-$dateStr-$formattedNum"
    }
}
