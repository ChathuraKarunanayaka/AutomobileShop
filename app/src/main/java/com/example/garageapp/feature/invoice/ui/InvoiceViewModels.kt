package com.example.garageapp.feature.invoice.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.garageapp.domain.model.Invoice
import com.example.garageapp.domain.model.JobCard
import com.example.garageapp.domain.model.JobCardItem
import com.example.garageapp.domain.model.WorkshopDetails
import com.example.garageapp.domain.usecase.CreateInvoiceUseCase
import com.example.garageapp.domain.usecase.GetInvoicesUseCase
import com.example.garageapp.domain.usecase.GetInvoiceByIdUseCase
import com.example.garageapp.domain.usecase.GetWorkshopDetailsUseCase
import com.example.garageapp.domain.usecase.GetJobCardItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoiceListViewModel @Inject constructor(
    private val getInvoicesUseCase: GetInvoicesUseCase
) : ViewModel() {
    private val _invoices = MutableStateFlow<List<Invoice>>(emptyList())
    val invoices: StateFlow<List<Invoice>> = _invoices.asStateFlow()

    init {
        getInvoicesUseCase().onEach { _invoices.value = it }.launchIn(viewModelScope)
    }
}

@HiltViewModel
class CreateInvoiceViewModel @Inject constructor(
    private val createInvoiceUseCase: CreateInvoiceUseCase
) : ViewModel() {
    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    fun createInvoice(
        jobCard: JobCard,
        items: List<JobCardItem>,
        discount: Double,
        paidAmount: Double,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (_isCreating.value) return
        _isCreating.value = true
        viewModelScope.launch {
            try {
                val invoice = createInvoiceUseCase(jobCard, items, discount, paidAmount)
                _isCreating.value = false
                onSuccess(invoice.invoiceId)
            } catch (e: Exception) {
                _isCreating.value = false
                onError(e.localizedMessage ?: "Failed to create invoice")
            }
        }
    }
}

@HiltViewModel
class InvoiceDetailsViewModel @Inject constructor(
    private val getInvoiceByIdUseCase: GetInvoiceByIdUseCase,
    private val getWorkshopDetailsUseCase: GetWorkshopDetailsUseCase,
    private val getJobCardItemsUseCase: GetJobCardItemsUseCase
) : ViewModel() {
    private val _invoice = MutableStateFlow<Invoice?>(null)
    val invoice: StateFlow<Invoice?> = _invoice.asStateFlow()

    private val _workshopDetails = MutableStateFlow(WorkshopDetails())
    val workshopDetails: StateFlow<WorkshopDetails> = _workshopDetails.asStateFlow()

    private val _items = MutableStateFlow<List<JobCardItem>>(emptyList())
    val items: StateFlow<List<JobCardItem>> = _items.asStateFlow()

    init {
        viewModelScope.launch {
            getWorkshopDetailsUseCase().collect { details ->
                details?.let { _workshopDetails.value = it }
            }
        }
    }

    fun loadInvoice(invoiceId: String) {
        viewModelScope.launch {
            val inv = getInvoiceByIdUseCase(invoiceId)
            _invoice.value = inv
            inv?.let {
                getJobCardItemsUseCase(it.jobCardId).collect { itemList ->
                    _items.value = itemList
                }
            }
        }
    }
}
