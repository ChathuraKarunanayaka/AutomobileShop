package com.example.garageapp.feature.jobcard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import com.example.garageapp.domain.model.JobCard
import com.example.garageapp.domain.model.JobCardItem
import com.example.garageapp.domain.model.JobCardItemType
import com.example.garageapp.domain.model.JobCardStatus

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
    var showEditJobCardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(jobCardId) {
        viewModel.loadJobCardDetails(jobCardId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(jobCard?.jobCardNumber ?: "Loading...", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(jobCard?.vehicleNumber ?: "", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    jobCard?.let { jc ->
                        IconButton(onClick = { showEditJobCardDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Job Details")
                        }
                        
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
                                Icon(Icons.Default.Receipt, contentDescription = "Create Invoice", tint = Color.White)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
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
        if (jobCard == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            ) {
                jobCard?.let { jc ->
                    JobDetailsCard(jc)
                }
                
                TotalSummary(items)

                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Repair & Service Items",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            text = "${items.size} ITEMS",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (items.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        Text("No items added yet", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    items.forEach { item ->
                        RepairItemRow(
                            item = item,
                            onDelete = { viewModel.deleteItem(item.itemId) },
                            enabled = jobCard?.status != JobCardStatus.COMPLETED && jobCard?.status != JobCardStatus.CANCELLED
                        )
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
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

    if (showEditJobCardDialog && jobCard != null) {
        EditJobCardDialog(
            jobCard = jobCard!!,
            onDismiss = { showEditJobCardDialog = false },
            onConfirm = { complaint, notes ->
                viewModel.updateJobCardDetails(complaint, notes)
                showEditJobCardDialog = false
            }
        )
    }
}

@Composable
fun JobDetailsCard(jobCard: JobCard) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complaint / Customer Issues", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            }
            Text(
                jobCard.complaintDescription, 
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                color = Color(0xFF212121),
                style = MaterialTheme.typography.bodyMedium
            )
            
            Divider(color = Color(0xFFF5F5F5))
            
            Row(modifier = Modifier.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Note, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Technical Inspection Notes", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
            }
            Text(
                jobCard.inspectionNotes.ifEmpty { "No technical notes added yet." }, 
                modifier = Modifier.padding(top = 8.dp),
                color = if (jobCard.inspectionNotes.isEmpty()) Color.Gray else Color(0xFF212121),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun TotalSummary(items: List<JobCardItem>) {
    val totalSellingPrice = items.sumOf { it.totalSellingPrice }
    val totalProfit = items.sumOf { it.profit }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A237E)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("TOTAL ESTIMATE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                Text("Rs. ${String.format("%,.0f", totalSellingPrice)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color.White)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("EST. PROFIT", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                Text("Rs. ${String.format("%,.0f", totalProfit)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
            }
        }
    }
}

@Composable
fun RepairItemRow(item: JobCardItem, onDelete: () -> Unit, enabled: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = when(item.itemType) {
                    JobCardItemType.LABOUR -> Color(0xFFE3F2FD)
                    JobCardItemType.SPARE_PART -> Color(0xFFE8F5E9)
                    else -> Color(0xFFFFF3E0)
                },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when(item.itemType) {
                            JobCardItemType.LABOUR -> Icons.Default.Build
                            JobCardItemType.SPARE_PART -> Icons.Default.Settings
                            else -> Icons.Default.Category
                        },
                        contentDescription = null,
                        tint = when(item.itemType) {
                            JobCardItemType.LABOUR -> Color(0xFF1976D2)
                            JobCardItemType.SPARE_PART -> Color(0xFF388E3C)
                            else -> Color(0xFFF57C00)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(item.description, fontWeight = FontWeight.Bold, color = Color(0xFF212121), fontSize = 15.sp)
                Text("${item.itemType.name} | Qty: ${item.quantity}", fontSize = 12.sp, color = Color.Gray)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("Rs.${String.format("%,.0f", item.totalSellingPrice)}", fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A237E), fontSize = 15.sp)
                if (enabled) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EditJobCardDialog(
    jobCard: JobCard,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var complaint by remember { mutableStateOf(jobCard.complaintDescription) }
    var notes by remember { mutableStateOf(jobCard.inspectionNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Job Details", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = complaint,
                    onValueChange = { complaint = it },
                    label = { Text("Complaint Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Technical Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(complaint, notes) }) { Text("UPDATE") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
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
        title = { Text("Add Repair/Service Item", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Item Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("Category", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 8.dp), 
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    JobCardItemType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name, fontSize = 11.sp) }
                        )
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        label = { Text("Unit Cost") },
                        modifier = Modifier.weight(2f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("Unit Selling Price") },
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
                else Text("ADD ITEM")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}

@Composable
fun StatusMenu(currentStatus: JobCardStatus, onStatusChange: (JobCardStatus) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val color = when (currentStatus) {
        JobCardStatus.OPEN -> Color(0xFF1976D2)
        JobCardStatus.IN_PROGRESS -> Color(0xFFF57C00)
        JobCardStatus.READY_FOR_DELIVERY -> Color(0xFF388E3C)
        else -> Color.White
    }
    
    Box {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(36.dp),
            shape = MaterialTheme.shapes.small
        ) {
            Text(currentStatus.name.replace("_", " "), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            JobCardStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.name.replace("_", " "), fontWeight = if(status == currentStatus) FontWeight.Bold else FontWeight.Normal) },
                    onClick = {
                        onStatusChange(status)
                        expanded = false
                    }
                )
            }
        }
    }
}
