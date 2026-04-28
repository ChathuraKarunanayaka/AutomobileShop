package com.example.garageapp.feature.jobcard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.garageapp.domain.model.JobCard
import com.example.garageapp.domain.model.JobCardItem
import com.example.garageapp.domain.model.JobCardItemType
import com.example.garageapp.domain.model.JobCardStatus
import com.example.garageapp.domain.model.WorkshopDetails
import com.example.garageapp.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class JobCardDetailsViewModel @Inject constructor(
    private val getJobCardByIdUseCase: GetJobCardByIdUseCase,
    private val getJobCardItemsUseCase: GetJobCardItemsUseCase,
    private val addJobCardItemUseCase: AddJobCardItemUseCase,
    private val deleteJobCardItemUseCase: DeleteJobCardItemUseCase,
    private val updateJobCardUseCase: UpdateJobCardUseCase,
    private val getWorkshopDetailsUseCase: GetWorkshopDetailsUseCase
) : ViewModel() {

    private val _jobCard = MutableStateFlow<JobCard?>(null)
    val jobCard: StateFlow<JobCard?> = _jobCard.asStateFlow()

    private val _items = MutableStateFlow<List<JobCardItem>>(emptyList())
    val items: StateFlow<List<JobCardItem>> = _items.asStateFlow()

    private val _workshopDetails = MutableStateFlow(WorkshopDetails())
    val workshopDetails: StateFlow<WorkshopDetails> = _workshopDetails.asStateFlow()

    private val _isSavingItem = MutableStateFlow(false)
    val isSavingItem: StateFlow<Boolean> = _isSavingItem.asStateFlow()

    init {
        viewModelScope.launch {
            getWorkshopDetailsUseCase().collect { details ->
                details?.let { _workshopDetails.value = it }
            }
        }
    }

    fun loadJobCardDetails(jobCardId: String) {
        viewModelScope.launch {
            val details = getJobCardByIdUseCase(jobCardId)
            _jobCard.value = details
            
            getJobCardItemsUseCase(jobCardId).collect {
                _items.value = it
            }
        }
    }

    fun addRepairItem(
        description: String,
        type: JobCardItemType,
        quantity: Int,
        costPrice: Double,
        sellingPrice: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentJobCard = _jobCard.value ?: return
        _isSavingItem.value = true
        viewModelScope.launch {
            try {
                val item = JobCardItem(
                    itemId = UUID.randomUUID().toString(),
                    shopId = currentJobCard.shopId,
                    jobCardId = currentJobCard.jobCardId,
                    description = description,
                    itemType = type,
                    quantity = quantity,
                    costPrice = costPrice,
                    sellingPrice = sellingPrice,
                    createdAt = System.currentTimeMillis()
                )
                addJobCardItemUseCase(item)
                _isSavingItem.value = false
                onSuccess()
            } catch (e: Exception) {
                _isSavingItem.value = false
                onError(e.localizedMessage ?: "Failed to add item")
            }
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            deleteJobCardItemUseCase(itemId)
        }
    }

    fun updateStatus(newStatus: JobCardStatus) {
        val current = _jobCard.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                status = newStatus, 
                updatedAt = System.currentTimeMillis(),
                completedAt = if (newStatus == JobCardStatus.COMPLETED) System.currentTimeMillis() else current.completedAt
            )
            updateJobCardUseCase(updated)
            _jobCard.value = updated
        }
    }
}
