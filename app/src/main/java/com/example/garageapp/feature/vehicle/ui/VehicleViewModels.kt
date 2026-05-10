package com.example.garageapp.feature.vehicle.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.garageapp.domain.model.Vehicle
import com.example.garageapp.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleListViewModel @Inject constructor(
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val getVehiclesByCustomerUseCase: GetVehiclesByCustomerUseCase,
    private val searchVehiclesUseCase: SearchVehiclesUseCase,
    private val deleteVehicleUseCase: DeleteVehicleUseCase
) : ViewModel() {
    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    private var searchJob: Job? = null
    private var loadJob: Job? = null
    private var isSearching = false

    fun loadVehiclesForCustomer(customerId: String) {
        isSearching = false
        loadJob?.cancel()
        loadJob = getVehiclesByCustomerUseCase(customerId).onEach { 
            if (!isSearching) _vehicles.value = it 
        }.launchIn(viewModelScope)
    }

    fun searchVehicles(query: String) {
        if (query.isBlank()) {
            return
        }

        isSearching = true
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                val results = searchVehiclesUseCase(query)
                _vehicles.value = results
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteVehicle(vehicleId: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isDeleting.value = true
            try {
                deleteVehicleUseCase(vehicleId)
                _isDeleting.value = false
                onSuccess()
            } catch (e: Exception) {
                _isDeleting.value = false
                onError(e.localizedMessage ?: "Failed to delete vehicle")
            }
        }
    }
}

@HiltViewModel
class AddVehicleViewModel @Inject constructor(
    private val addVehicleUseCase: AddVehicleUseCase,
    private val getVehicleByIdUseCase: GetVehicleByIdUseCase,
    private val updateVehicleUseCase: UpdateVehicleUseCase
) : ViewModel() {
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _vehicle = MutableStateFlow<Vehicle?>(null)
    val vehicle: StateFlow<Vehicle?> = _vehicle.asStateFlow()

    fun loadVehicle(vehicleId: String) {
        viewModelScope.launch {
            val vehicle = getVehicleByIdUseCase(vehicleId)
            _vehicle.value = vehicle
        }
    }

    fun addVehicle(vehicle: Vehicle, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_isSaving.value) return
        _isSaving.value = true
        viewModelScope.launch {
            try {
                addVehicleUseCase(vehicle)
                _isSaving.value = false
                onSuccess()
            } catch (e: Exception) {
                _isSaving.value = false
                onError(e.localizedMessage ?: "Failed to save vehicle")
            }
        }
    }

    fun updateVehicle(vehicle: Vehicle, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_isSaving.value) return
        _isSaving.value = true
        viewModelScope.launch {
            try {
                updateVehicleUseCase(vehicle)
                _isSaving.value = false
                onSuccess()
            } catch (e: Exception) {
                _isSaving.value = false
                onError(e.localizedMessage ?: "Failed to update vehicle")
            }
        }
    }
}
