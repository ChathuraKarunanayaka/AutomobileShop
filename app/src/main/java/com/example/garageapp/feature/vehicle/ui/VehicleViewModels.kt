package com.example.garageapp.feature.vehicle.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.garageapp.domain.model.Vehicle
import com.example.garageapp.domain.usecase.AddVehicleUseCase
import com.example.garageapp.domain.usecase.GetVehiclesUseCase
import com.example.garageapp.domain.usecase.SearchVehiclesUseCase
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
    private val searchVehiclesUseCase: SearchVehiclesUseCase
) : ViewModel() {
    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    private var searchJob: Job? = null
    private var isSearching = false

    init {
        loadAllVehicles()
    }

    private fun loadAllVehicles() {
        isSearching = false
        getVehiclesUseCase().onEach { 
            if (!isSearching) _vehicles.value = it 
        }.launchIn(viewModelScope)
    }

    fun searchVehicles(query: String) {
        if (query.isBlank()) {
            loadAllVehicles()
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
}

@HiltViewModel
class AddVehicleViewModel @Inject constructor(
    private val addVehicleUseCase: AddVehicleUseCase
) : ViewModel() {
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

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
}
