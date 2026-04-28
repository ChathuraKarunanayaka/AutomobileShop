package com.example.garageapp.feature.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onCustomersClick: () -> Unit = {},
    onVehiclesClick: () -> Unit = {},
    onJobCardsClick: () -> Unit = {},
    onInvoicesClick: () -> Unit = {},
    onReportsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val stats by viewModel.stats.collectAsState()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("GARAGE PRO", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Operational Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardStatCard("Active Jobs", stats.openJobs.toString(), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                DashboardStatCard("Completed", stats.completedJobs.toString(), Color(0xFF2E7D32), Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardStatCard("Today's Rev.", "Rs.${stats.todaysIncome.toInt()}", Color(0xFFE65100), Modifier.weight(1f))
                DashboardStatCard("Outstanding", "Rs.${stats.pendingPayments.toInt()}", Color(0xFFC62828), Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            ActionGrid(
                listOf(
                    ActionItem("Customers", Icons.Default.People, onCustomersClick),
                    ActionItem("Vehicles", Icons.Default.DirectionsCar, onVehiclesClick),
                    ActionItem("Job Cards", Icons.Default.Assignment, onJobCardsClick),
                    ActionItem("Invoices", Icons.Default.Receipt, onInvoicesClick),
                    ActionItem("Business Reports", Icons.Default.BarChart, onReportsClick)
                )
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun DashboardStatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text(text = title.uppercase(), color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

data class ActionItem(val title: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
fun ActionGrid(items: List<ActionItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { item ->
            Button(
                onClick = item.onClick,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = MaterialTheme.shapes.medium,
                elevation = ButtonDefaults.buttonElevation(2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(item.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(item.title, color = Color(0xFF212121), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                }
            }
        }
    }
}
