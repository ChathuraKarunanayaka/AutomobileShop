package com.example.garageapp.feature.customer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.garageapp.domain.model.Customer
import com.example.garageapp.domain.usecase.AddCustomerUseCase
import com.example.garageapp.domain.usecase.GetCustomersUseCase
import com.example.garageapp.domain.usecase.SearchCustomersUseCase
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
class CustomerListViewModel @Inject constructor(
    private val getCustomersUseCase: GetCustomersUseCase,
    private val searchCustomersUseCase: SearchCustomersUseCase
) : ViewModel() {
    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private var searchJob: Job? = null
    private var isSearching = false

    init {
        loadAllCustomers()
    }

    private fun loadAllCustomers() {
        isSearching = false
        getCustomersUseCase().onEach { 
            if (!isSearching) _customers.value = it 
        }.launchIn(viewModelScope)
    }

    fun searchCustomers(query: String) {
        if (query.isBlank()) {
            loadAllCustomers()
            return
        }

        isSearching = true
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                val results = searchCustomersUseCase(query)
                _customers.value = results
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

@HiltViewModel
class AddCustomerViewModel @Inject constructor(
    private val addCustomerUseCase: AddCustomerUseCase
) : ViewModel() {
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun addCustomer(customer: Customer, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_isSaving.value) return
        
        _isSaving.value = true
        viewModelScope.launch {
            try {
                // The usecase calls repository which calls .set().await()
                // If it succeeds, it means it reached Firestore (or at least local cache with pending sync)
                addCustomerUseCase(customer)
                _isSaving.value = false
                onSuccess()
            } catch (e: Exception) {
                _isSaving.value = false
                onError(e.localizedMessage ?: "Failed to save customer. Please check your connection.")
            }
        }
    }
}
