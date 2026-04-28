package com.example.garageapp.feature.jobcard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.garageapp.core.util.PdfGenerator
import com.example.garageapp.core.util.ShareUtils
import com.example.garageapp.domain.model.JobCardItem
import com.example.garageapp.domain.model.JobCardItemType
import com.example.garageapp.domain.model.JobCardStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobCardDetailsScreen(
    jobCardId: String,
    onBack: () -> Unit,
    onCreateInvoice: () -> Unit,
    viewModel: JobCardDetailsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val jobCard by viewModel.jobCard.collectAsState()
    val items by viewModel.items.collectAsState()
    val workshop by viewModel.workshopDetails.collectAsState()
    var showAddItemDialog by remember { mutableStateOf(false) }

    LaunchedEffect(jobCardId) {
        viewModel.loadJobCardDetails(jobCardId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(jobCard?.jobCardNumber ?: "Loading...", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(jobCard?.vehicleNumber ?: "", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    jobCard?.let { jc ->
                        var expandedAction by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { expandedAction = true }) {
                                Icon(Icons.Default.Share, contentDescription = "Share")
                            }
                            DropdownMenu(expanded = expandedAction, onDismissRequest = { expandedAction = false }) {
                                DropdownMenuItem(
                                    text = { Text("Share Text Summary") },
                                    onClick = {
                                        val summary = "Dear customer, your job card ${jc.jobCardNumber} has been created for vehicle ${jc.vehicleNumber}. We will update you once the repair is completed."
                                        ShareUtils.shareText(context, summary, jc.customerPhone)
                                        expandedAction = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Share PDF Document") },
                                    onClick = {
                                        val file = PdfGenerator.generateJobCardPdf(context, jc, workshop)
                                        file?.let { ShareUtils.shareFile(context, it, jc.customerPhone) }
                                        expandedAction = false
                                    }
                                )
                            }
                        }
                        
                        StatusMenu(
                            currentStatus = jc.status,
                            onStatusChange = { viewModel.updateStatus(it) }
                        )
                        if (jc.status == JobCardStatus.COMPLETED) {
                            IconButton(onClick = onCreateInvoice) {
                                Icon(Icons.Default.Receipt, contentDescription = "Create Invoice", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            if (jobCard?.status != JobCardStatus.COMPLETED && jobCard?.status != JobCardStatus.CANCELLED) {
                FloatingActionButton(
                    onClick = { showAddItemDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header with totals
            TotalSummary(items)

            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No items added yet", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items) { item ->
                        RepairItemRow(
                            item = item,
                            onDelete = { viewModel.deleteItem(item.itemId) },
                            enabled = jobCard?.status != JobCardStatus.COMPLETED && jobCard?.status != JobCardStatus.CANCELLED
                        )
                    }
                }
            }
        }
    }

    if (showAddItemDialog) {
        AddItemDialog(
            onDismiss = { showAddItemDialog = false },
            onConfirm = { desc, type, qty, cost, sell ->
                viewModel.addRepairItem(desc, type, qty, cost, sell, 
                    onSuccess = { showAddItemDialog = false },
                    onError = { /* Handle error */ }
                )
            },
            isSaving = viewModel.isSavingItem.collectAsState().value
        )
    }
}

@Composable
fun TotalSummary(items: List<JobCardItem>) {
    val totalSellingPrice = items.sumOf { it.totalSellingPrice }
    val totalProfit = items.sumOf { it.profit }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Estimated Total", style = MaterialTheme.typography.bodySmall)
                Text("Rs. ${totalSellingPrice.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Estimated Profit", style = MaterialTheme.typography.bodySmall)
                Text("Rs. ${totalProfit.toInt()}", style = MaterialTheme.typography.titleMedium, color = Color(0xFF388E3C))
            }
        }
    }
}

@Composable
fun RepairItemRow(item: JobCardItem, onDelete: () -> Unit, enabled: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.description, fontWeight = FontWeight.Medium, color = Color.Black)
                Text("${item.itemType.name} | Qty: ${item.quantity}", fontSize = 12.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text("Rs. ${item.totalSellingPrice.toInt()}", fontWeight = FontWeight.Bold, color = Color.Black)
            }
            if (enabled) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, JobCardItemType, Int, Double, Double) -> Unit,
    isSaving: Boolean
) {
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(JobCardItemType.SPARE_PART) }
    var quantity by remember { mutableStateOf("1") }
    var costPrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Repair Item") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Item Type", style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), 
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    JobCardItemType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name, fontSize = 10.sp) }
                        )
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { if (it.all { char -> char.isDigit() }) quantity = it },
                        label = { Text("Qty") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = costPrice,
                        onValueChange = { costPrice = it },
                        label = { Text("Cost") },
                        modifier = Modifier.weight(2f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                    )
                }
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("Selling Price") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(
                        description, 
                        selectedType, 
                        quantity.toIntOrNull() ?: 1, 
                        costPrice.toDoubleOrNull() ?: 0.0, 
                        sellingPrice.toDoubleOrNull() ?: 0.0
                    ) 
                },
                enabled = !isSaving && description.isNotBlank() && sellingPrice.isNotBlank()
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun StatusMenu(currentStatus: JobCardStatus, onStatusChange: (JobCardStatus) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        TextButton(onClick = { expanded = true }) {
            Text(currentStatus.name, color = MaterialTheme.colorScheme.primary)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            JobCardStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.name) },
                    onClick = {
                        onStatusChange(status)
                        expanded = false
                    }
                )
            }
        }
    }
}
