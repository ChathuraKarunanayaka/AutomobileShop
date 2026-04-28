package com.example.garageapp.domain.usecase

import com.example.garageapp.domain.model.Customer
import com.example.garageapp.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCustomersUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) {
    operator fun invoke(): Flow<List<Customer>> = customerRepository.getCustomers()
}

class GetCustomerByIdUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) {
    suspend operator fun invoke(id: String): Customer? = customerRepository.getCustomerById(id)
}

class AddCustomerUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) {
    suspend operator fun invoke(customer: Customer) = customerRepository.addCustomer(customer)
}

class UpdateCustomerUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) {
    suspend operator fun invoke(customer: Customer) = customerRepository.updateCustomer(customer)
}

class SearchCustomersUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) {
    suspend operator fun invoke(query: String): List<Customer> = customerRepository.searchCustomers(query)
}
