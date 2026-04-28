package com.example.garageapp.feature.report.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
    val dailyStats by viewModel.dailyStats.collectAsState()
    val periodStats by viewModel.periodStats.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & Profit") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F7FA))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(selectedDate))
            Text(
                text = "Summary for $dateStr",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
            }

            dailyStats?.let { stats ->
                ReportSummaryGrid(stats)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Income Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                BreakdownCard(stats)

            } ?: run {
                if (!isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No data available for this date.", color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Profit Trend (Last 30 Days)",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Card(
                modifier = Modifier.fillMaxWidth().height(250.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    ProfitChart(stats = periodStats)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ReportSummaryGrid(stats: DailyStats) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReportCard("Total Sales", "Rs. ${stats.totalSales.toInt()}", Color(0xFF1A237E), Modifier.weight(1f))
            ReportCard("Total Profit", "Rs. ${stats.totalProfit.toInt()}", Color(0xFF388E3C), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReportCard("Payments Rec.", "Rs. ${stats.totalPaid.toInt()}", Color(0xFFFB8C00), Modifier.weight(1f))
            ReportCard("Pending", "Rs. ${stats.pendingBalance.toInt()}", Color(0xFFD32F2F), Modifier.weight(1f))
        }
    }
}

@Composable
fun BreakdownCard(stats: DailyStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BreakdownRow("Labor Charges", "Rs. ${stats.laborCharges.toInt()}", Color(0xFF3949AB))
            BreakdownRow("Spare Parts", "Rs. ${stats.sparePartsCost.toInt()}", Color(0xFF43A047))
            BreakdownRow("Outside Purchases", "Rs. ${stats.outsidePurchases.toInt()}", Color(0xFFFB8C00))
            Divider()
            BreakdownRow("Total Sales", "Rs. ${stats.totalSales.toInt()}", Color.Black, isBold = true)
        }
    }
}

@Composable
fun BreakdownRow(label: String, value: String, color: Color, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontSize = 14.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium, color = color)
    }
}

@Composable
fun ReportCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
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
fun ProfitChart(stats: List<DailyStats>) {
    if (stats.size < 2) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Need more data for chart...", color = Color.Gray, fontSize = 12.sp)
        }
        return
    }

    val maxProfit = stats.maxOfOrNull { it.totalProfit }?.coerceAtLeast(100.0) ?: 100.0
    val minProfit = stats.minOfOrNull { it.totalProfit }?.coerceAtMost(0.0) ?: 0.0
    val range = (maxProfit - minProfit).coerceAtLeast(1.0)
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val spacing = width / (stats.size - 1)
        
        val path = Path()
        stats.forEachIndexed { index, dailyStats ->
            val x = index * spacing
            val normalizedY = (dailyStats.totalProfit - minProfit) / range
            val y = height - (normalizedY.toFloat() * height)
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            
            drawCircle(
                color = Color(0xFF388E3C), 
                radius = 3.dp.toPx(), 
                center = Offset(x, y)
            )
        }
        
        drawPath(
            path = path,
            color = Color(0xFF388E3C),
            style = Stroke(width = 2.dp.toPx())
        )
        
        // Baseline (Zero profit line if range includes zero)
        if (minProfit < 0 && maxProfit > 0) {
            val zeroY = height - ((-minProfit / range).toFloat() * height)
            drawLine(
                color = Color.LightGray,
                start = Offset(0f, zeroY),
                end = Offset(width, zeroY),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}
