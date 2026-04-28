package com.example.garageapp.domain.usecase

import com.example.garageapp.domain.model.Vehicle
import com.example.garageapp.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVehiclesUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    operator fun invoke(): Flow<List<Vehicle>> = vehicleRepository.getVehicles()
}

class GetVehiclesByCustomerUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    operator fun invoke(customerId: String): Flow<List<Vehicle>> = 
        vehicleRepository.getVehiclesByCustomer(customerId)
}

class AddVehicleUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(vehicle: Vehicle) = vehicleRepository.addVehicle(vehicle)
}

class SearchVehiclesUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(query: String): List<Vehicle> = vehicleRepository.searchVehicles(query)
}
