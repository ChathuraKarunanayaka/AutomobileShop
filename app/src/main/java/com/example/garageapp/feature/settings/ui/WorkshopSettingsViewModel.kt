package com.example.garageapp.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.garageapp.domain.model.WorkshopDetails
import com.example.garageapp.domain.usecase.GetWorkshopDetailsUseCase
import com.example.garageapp.domain.usecase.UpdateWorkshopDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkshopSettingsViewModel @Inject constructor(
    private val getWorkshopDetailsUseCase: GetWorkshopDetailsUseCase,
    private val updateWorkshopDetailsUseCase: UpdateWorkshopDetailsUseCase
) : ViewModel() {

    private val _workshopDetails = MutableStateFlow(WorkshopDetails())
    val workshopDetails: StateFlow<WorkshopDetails> = _workshopDetails.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init {
        viewModelScope.launch {
            getWorkshopDetailsUseCase().collect { details ->
                details?.let { _workshopDetails.value = it }
            }
        }
    }

    fun updateDetails(
        name: String,
        address: String,
        phoneNumber: String,
        email: String,
        footerNote: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _isSaving.value = true
        viewModelScope.launch {
            try {
                val updated = _workshopDetails.value.copy(
                    name = name,
                    address = address,
                    phoneNumber = phoneNumber,
                    email = email,
                    footerNote = footerNote,
                    updatedAt = System.currentTimeMillis()
                )
                updateWorkshopDetailsUseCase(updated)
                _isSaving.value = false
                onSuccess()
            } catch (e: Exception) {
                _isSaving.value = false
                onError(e.localizedMessage ?: "Failed to update settings")
            }
        }
    }
}
