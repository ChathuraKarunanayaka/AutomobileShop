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
            } ?: run {
                if (!isLoading) {
                    Text("No data available for this date.", modifier = Modifier.padding(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Profit Trend (Last 7 Days)",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Card(
                modifier = Modifier.fillMaxWidth().height(250.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    // Simple Chart Placeholder/Implementation
                    ProfitChart(stats = periodStats.takeLast(7))
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
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReportCard("Invoices", stats.invoiceCount.toString(), Color(0xFF455A64), Modifier.weight(1f))
            ReportCard("Job Cards", stats.completedJobCards.toString(), Color(0xFF455A64), Modifier.weight(1f))
        }
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
    if (stats.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Insufficient data for chart", color = Color.Gray)
        }
        return
    }

    val maxProfit = stats.maxOfOrNull { it.totalProfit }?.coerceAtLeast(100.0) ?: 100.0
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val spacing = width / (stats.size.coerceAtLeast(2) - 1).coerceAtLeast(1)
        
        val path = Path()
        stats.forEachIndexed { index, dailyStats ->
            val x = index * spacing
            val y = height - (dailyStats.totalProfit.toFloat() / maxProfit.toFloat() * height)
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            
            drawCircle(color = Color(0xFF388E3C), radius = 4.dp.toPx(), center = Offset(x, y))
        }
        
        drawPath(
            path = path,
            color = Color(0xFF388E3C),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
