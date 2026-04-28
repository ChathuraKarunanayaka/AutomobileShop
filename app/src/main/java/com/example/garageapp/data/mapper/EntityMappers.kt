package com.example.garageapp.data.mapper

import com.example.garageapp.data.model.*
import com.example.garageapp.domain.model.*

fun CustomerEntity.toDomain() = Customer(
    customerId = customerId,
    shopId = shopId,
    name = name,
    phoneNumber = phoneNumber,
    address = address,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Customer.toEntity() = CustomerEntity(
    customerId = customerId,
    shopId = shopId,
    name = name,
    phoneNumber = phoneNumber,
    address = address,
    searchKeywords = emptyList(),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun VehicleEntity.toDomain() = Vehicle(
    vehicleId, shopId, customerId, vehicleNumber, model, notes, createdAt, updatedAt
)

fun Vehicle.toEntity() = VehicleEntity(
    vehicleId, shopId, customerId, vehicleNumber, model, notes, createdAt, updatedAt
)

fun JobCardEntity.toDomain() = JobCard(
    jobCardId, shopId, jobCardNumber, customerId, vehicleId, customerName, customerPhone, vehicleNumber,
    JobCardStatus.valueOf(status.name), complaintDescription, inspectionNotes, createdAt, updatedAt, completedAt
)

fun JobCard.toEntity() = JobCardEntity(
    jobCardId, shopId, jobCardNumber, customerId, vehicleId, customerName, customerPhone, vehicleNumber,
    JobCardStatusEntity.valueOf(status.name), complaintDescription, inspectionNotes, createdAt, updatedAt, completedAt
)

fun JobCardItemEntity.toDomain() = JobCardItem(
    itemId, shopId, jobCardId, description, JobCardItemType.valueOf(itemType.name), quantity, costPrice, sellingPrice, totalCost, totalSellingPrice, profit, createdAt, updatedAt
)

fun JobCardItem.toEntity() = JobCardItemEntity(
    itemId, shopId, jobCardId, description, JobCardItemTypeEntity.valueOf(itemType.name), quantity, costPrice, sellingPrice, totalCost, totalSellingPrice, profit, createdAt, updatedAt
)

fun InvoiceEntity.toDomain() = Invoice(
    invoiceId, shopId, invoiceNumber, jobCardId, customerId, vehicleId, customerName, customerPhone, vehicleNumber,
    subtotal, discount, totalAmount, paidAmount, balanceAmount, PaymentStatus.valueOf(paymentStatus.name), createdAt, updatedAt
)

fun Invoice.toEntity() = InvoiceEntity(
    invoiceId, shopId, invoiceNumber, jobCardId, customerId, vehicleId, customerName, customerPhone, vehicleNumber,
    subtotal, discount, totalAmount, paidAmount, balanceAmount, PaymentStatusEntity.valueOf(paymentStatus.name), createdAt, updatedAt
)

fun PaymentEntity.toDomain() = Payment(
    paymentId, shopId, invoiceId, amount, PaymentMethod.valueOf(paymentMethod.name), note, createdAt
)

fun Payment.toEntity() = PaymentEntity(
    paymentId, shopId, invoiceId, amount, PaymentMethodEntity.valueOf(paymentMethod.name), note, createdAt
)

fun UserEntity.toDomain() = User(
    userId, shopId, name, email, role, createdAt
)

fun User.toEntity() = UserEntity(
    userId, shopId, name, email, role, createdAt
)

fun CounterEntity.toDomain() = Counter(
    counterId, shopId, jobCardNextNumber, invoiceNextNumber
)

fun Counter.toEntity() = CounterEntity(
    counterId, shopId, jobCardNextNumber, invoiceNextNumber
)
