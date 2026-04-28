package com.example.garageapp.feature.invoice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.garageapp.feature.jobcard.ui.JobCardDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(
    jobCardId: String,
    onBack: () -> Unit,
    onInvoiceCreated: (String) -> Unit,
    viewModel: CreateInvoiceViewModel = hiltViewModel(),
    jobCardViewModel: JobCardDetailsViewModel = hiltViewModel()
) {
    val jobCard by jobCardViewModel.jobCard.collectAsState()
    val items by jobCardViewModel.items.collectAsState()
    val isCreating by viewModel.isCreating.collectAsState()

    var discount by remember { mutableStateOf("") }
    var paidAmount by remember { mutableStateOf("") }

    LaunchedEffect(jobCardId) {
        jobCardViewModel.loadJobCardDetails(jobCardId)
    }

    val subtotal = items.sumOf { it.totalSellingPrice }
    val discountVal = discount.toDoubleOrNull() ?: 0.0
    val total = subtotal - discountVal
    val paidVal = paidAmount.toDoubleOrNull() ?: 0.0
    val balance = total - paidVal

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Invoice") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (jobCard == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F7FA))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Vehicle: ${jobCard?.vehicleNumber}", fontWeight = FontWeight.Bold)
                        Text("Customer: ${jobCard?.customerName}", fontSize = 14.sp, color = Color.Gray)
                        Text("Job Card: ${jobCard?.jobCardNumber}", fontSize = 14.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Summary", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                
                InvoiceSummaryRow("Subtotal", "Rs. ${subtotal.toInt()}")
                
                OutlinedTextField(
                    value = discount,
                    onValueChange = { discount = it },
                    label = { Text("Discount") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                )

                InvoiceSummaryRow("Total Amount", "Rs. ${total.toInt()}", isBold = true)

                OutlinedTextField(
                    value = paidAmount,
                    onValueChange = { paidAmount = it },
                    label = { Text("Paid Amount") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                )

                InvoiceSummaryRow("Balance", "Rs. ${balance.toInt()}", color = if (balance > 0) Color.Red else Color(0xFF388E3C))

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        jobCard?.let { jc ->
                            viewModel.createInvoice(
                                jobCard = jc,
                                items = items,
                                discount = discountVal,
                                paidAmount = paidVal,
                                onSuccess = onInvoiceCreated,
                                onError = { /* Show snackbar */ }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isCreating
                ) {
                    if (isCreating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Generate Invoice & Save")
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceSummaryRow(label: String, value: String, isBold: Boolean = false, color: Color = Color.Black) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, color = color)
    }
}
