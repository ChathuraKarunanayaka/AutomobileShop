package com.example.garageapp.feature.jobcard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.garageapp.domain.model.JobCard
import com.example.garageapp.domain.model.JobCardStatus
import com.example.garageapp.domain.usecase.AddJobCardUseCase
import com.example.garageapp.domain.usecase.GenerateJobCardNumberUseCase
import com.example.garageapp.domain.usecase.GetJobCardsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class JobCardListViewModel @Inject constructor(
    private val getJobCardsUseCase: GetJobCardsUseCase
) : ViewModel() {
    private val _jobCards = MutableStateFlow<List<JobCard>>(emptyList())
    val jobCards: StateFlow<List<JobCard>> = _jobCards.asStateFlow()

    init {
        getJobCardsUseCase().onEach { _jobCards.value = it }.launchIn(viewModelScope)
    }
}

@HiltViewModel
class CreateJobCardViewModel @Inject constructor(
    private val addJobCardUseCase: AddJobCardUseCase,
    private val generateJobCardNumberUseCase: GenerateJobCardNumberUseCase
) : ViewModel() {
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _generatedNumber = MutableStateFlow("")
    val generatedNumber: StateFlow<String> = _generatedNumber.asStateFlow()

    init {
        viewModelScope.launch {
            _generatedNumber.value = generateJobCardNumberUseCase()
        }
    }

    fun createJobCard(
        customerId: String,
        customerName: String,
        customerPhone: String,
        vehicleId: String,
        vehicleNumber: String,
        complaint: String,
        notes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (_isSaving.value) return
        _isSaving.value = true
        viewModelScope.launch {
            try {
                val jobCard = JobCard(
                    jobCardId = UUID.randomUUID().toString(),
                    jobCardNumber = _generatedNumber.value,
                    customerId = customerId,
                    vehicleId = vehicleId,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    vehicleNumber = vehicleNumber,
                    status = JobCardStatus.OPEN,
                    complaintDescription = complaint,
                    inspectionNotes = notes,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                addJobCardUseCase(jobCard)
                _isSaving.value = false
                onSuccess()
            } catch (e: Exception) {
                _isSaving.value = false
                onError(e.localizedMessage ?: "Failed to create job card")
            }
        }
    }
}
