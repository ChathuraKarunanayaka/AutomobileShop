package com.example.garageapp.feature.report.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.garageapp.domain.model.DailyStats
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    onBack: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val periodStats by viewModel.periodStats.collectAsState()
    val periodSummary by viewModel.periodSummary.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showRangePicker by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()

    if (showRangePicker) {
        DateRangePickerDialog(
            state = dateRangePickerState,
            onDismiss = { showRangePicker = false },
            onConfirm = {
                val start = dateRangePickerState.selectedStartDateMillis
                val end = dateRangePickerState.selectedEndDateMillis
                if (start != null && end != null) {
                    viewModel.loadStatsForPeriod(start, end)
                }
                showRangePicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Reports", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showRangePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Range")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Period Range Display
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val rangeText = if (startDate != null && endDate != null) {
                "${sdf.format(Date(startDate!!))} - ${sdf.format(Date(endDate!!))}"
            } else {
                "Select Date Range"
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(rangeText, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Financial Summary
            Text("Financial Summary", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReportStatCard("Total Sales", "Rs. ${periodSummary.totalSales.toInt()}", Color(0xFF1A237E), Modifier.weight(1f))
                    ReportStatCard("Total Profit", "Rs. ${periodSummary.totalProfit.toInt()}", Color(0xFF2E7D32), Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReportStatCard("Collections", "Rs. ${periodSummary.totalPaid.toInt()}", Color(0xFFE65100), Modifier.weight(1f))
                    ReportStatCard("Outstanding", "Rs. ${periodSummary.pendingBalance.toInt()}", Color(0xFFC62828), Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Breakdown
            Text("Category Breakdown", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryRow("Labor Income", "Rs. ${periodSummary.laborCharges.toInt()}")
                    SummaryRow("Parts Income", "Rs. ${periodSummary.sparePartsCost.toInt()}")
                    SummaryRow("Outside Purchases", "Rs. ${periodSummary.outsidePurchases.toInt()}")
                    Divider()
                    SummaryRow("Gross Total", "Rs. ${periodSummary.totalSales.toInt()}", isBold = true)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Daily Details List
            if (periodStats.isNotEmpty()) {
                Text("Daily Details", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                
                periodStats.reversed().forEach { stats ->
                    DailyStatItem(stats)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else if (!isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No transactions found for this period.", color = Color.LightGray)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    state: DateRangePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DateRangePicker(
            state = state,
            title = { Text("Select Date Range", modifier = Modifier.padding(16.dp)) },
            modifier = Modifier.height(450.dp)
        )
    }
}

@Composable
fun ReportStatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 12.sp, color = Color.Gray)
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
fun DailyStatItem(stats: DailyStats) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(sdf.format(Date(stats.date)), fontWeight = FontWeight.Bold)
                Text("${stats.invoiceCount} Invoices", fontSize = 12.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Rs. ${stats.totalSales.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                Text("Profit: Rs. ${stats.totalProfit.toInt()}", fontSize = 12.sp, color = Color(0xFF2E7D32))
            }
        }
    }
}
