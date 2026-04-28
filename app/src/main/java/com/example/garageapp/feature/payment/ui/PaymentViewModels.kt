package com.example.garageapp.feature.payment.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.garageapp.domain.model.Payment
import com.example.garageapp.domain.model.PaymentMethod
import com.example.garageapp.domain.repository.PaymentRepository
import com.example.garageapp.domain.repository.InvoiceRepository
import com.example.garageapp.domain.model.PaymentStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddPaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun addPayment(
        invoiceId: String,
        amount: Double,
        method: PaymentMethod,
        note: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (_isSaving.value) return
        _isSaving.value = true
        viewModelScope.launch {
            try {
                // 1. Add payment record
                val payment = Payment(
                    paymentId = UUID.randomUUID().toString(),
                    invoiceId = invoiceId,
                    amount = amount,
                    paymentMethod = method,
                    note = note,
                    createdAt = System.currentTimeMillis()
                )
                paymentRepository.addPayment(payment)

                // 2. Update invoice paid amount and status
                val invoice = invoiceRepository.getInvoiceById(invoiceId)
                if (invoice != null) {
                    val newPaidAmount = invoice.paidAmount + amount
                    val newBalance = invoice.totalAmount - newPaidAmount
                    val newStatus = when {
                        newPaidAmount >= invoice.totalAmount -> PaymentStatus.PAID
                        newPaidAmount > 0 -> PaymentStatus.PARTIALLY_PAID
                        else -> PaymentStatus.UNPAID
                    }
                    val updatedInvoice = invoice.copy(
                        paidAmount = newPaidAmount,
                        balanceAmount = newBalance,
                        paymentStatus = newStatus,
                        updatedAt = System.currentTimeMillis()
                    )
                    invoiceRepository.updateInvoice(updatedInvoice)
                }
                
                _isSaving.value = false
                onSuccess()
            } catch (e: Exception) {
                _isSaving.value = false
                onError(e.localizedMessage ?: "Failed to add payment")
            }
        }
    }
}
