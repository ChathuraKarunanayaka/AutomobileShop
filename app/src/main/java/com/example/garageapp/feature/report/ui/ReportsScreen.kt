package com.example.garageapp.feature.report.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onBack: () -> Unit,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val reportData by viewModel.reportData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // Default to last 30 days
    val calendar = Calendar.getInstance()
    val endDate = calendar.timeInMillis
    calendar.add(Calendar.DAY_OF_YEAR, -30)
    val startDate = calendar.timeInMillis
    
    LaunchedEffect(Unit) {
        viewModel.generateReport(startDate, endDate)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Reports") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
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
                Text(
                    "Summary (Last 30 Days)",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReportStatCard("Total Sales", "Rs. ${reportData.totalSales.toInt()}", Color(0xFF1A237E), Modifier.weight(1f))
                    ReportStatCard("Total Paid", "Rs. ${reportData.totalPaid.toInt()}", Color(0xFF43A047), Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReportStatCard("Discounts", "Rs. ${reportData.totalDiscount.toInt()}", Color(0xFFFB8C00), Modifier.weight(1f))
                    ReportStatCard("Balance Due", "Rs. ${reportData.pendingBalance.toInt()}", Color(0xFFD32F2F), Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Detailed Stats", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                        
                        DetailRow("Number of Invoices", reportData.invoiceCount.toString())
                        DetailRow("Average Invoice Value", if (reportData.invoiceCount > 0) "Rs. ${(reportData.totalSales / reportData.invoiceCount).toInt()}" else "Rs. 0")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Sales Trend (Last 7 Days)", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                Card(
                    modifier = Modifier.fillMaxWidth().height(250.dp).padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    if (reportData.dailyStats.isNotEmpty()) {
                        SimpleBarChart(reportData.dailyStats)
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No data available for chart", color = Color.LightGray)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SimpleBarChart(dailyStats: Map<String, Double>) {
    val sortedData = dailyStats.toList().sortedBy { it.first }.takeLast(7)
    val maxVal = (sortedData.maxByOrNull { it.second }?.second ?: 1.0).toFloat()
    
    Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 32.dp)) {
        val barWidth = size.width / (sortedData.size * 2f)
        val space = size.width / (sortedData.size * 2f)
        
        sortedData.forEachIndexed { index, data ->
            val barHeight = (data.second.toFloat() / maxVal) * size.height
            val x = index * (barWidth + space) + space / 2
            
            drawRect(
                color = Color(0xFF3949AB),
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barWidth, barHeight)
            )
            
            // Draw labels using native canvas for simplicity
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 30f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(data.first, x + barWidth / 2, size.height + 30f, paint)
                
                if (data.second > 0) {
                    drawText("${data.second.toInt()}", x + barWidth / 2, size.height - barHeight - 10f, paint)
                }
            }
        }
    }
}

@Composable
fun ReportStatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(90.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = title, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            Text(text = value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Medium)
    }
}
