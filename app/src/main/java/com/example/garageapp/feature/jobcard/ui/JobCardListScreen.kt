package com.example.garageapp.feature.jobcard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.garageapp.domain.model.JobCard
import com.example.garageapp.domain.model.JobCardStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobCardListScreen(
    onBack: () -> Unit,
    onJobCardClick: (String) -> Unit,
    viewModel: JobCardListViewModel = hiltViewModel()
) {
    val jobCards by viewModel.jobCards.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredJobCards = remember(jobCards, searchQuery) {
        if (searchQuery.isEmpty()) {
            jobCards
        } else {
            jobCards.filter { 
                it.jobCardNumber.contains(searchQuery, ignoreCase = true) ||
                it.customerName.contains(searchQuery, ignoreCase = true) ||
                it.vehicleNumber.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Cards") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search by No, Name or Vehicle") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredJobCards) { jobCard ->
                    JobCardItem(
                        jobCard = jobCard,
                        onClick = { onJobCardClick(jobCard.jobCardId) }
                    )
                }
            }
        }
    }
}

@Composable
fun JobCardItem(jobCard: JobCard, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = jobCard.jobCardNumber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                JobCardStatusChip(status = jobCard.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(text = jobCard.vehicleNumber, fontWeight = FontWeight.Medium, fontSize = 15.sp)
            Text(text = jobCard.customerName, fontSize = 14.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val date = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(jobCard.createdAt))
            Text(text = "Created: $date", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun JobCardStatusChip(status: JobCardStatus) {
    val color = when (status) {
        JobCardStatus.OPEN -> Color(0xFF2196F3)
        JobCardStatus.IN_PROGRESS -> Color(0xFFFFA000)
        JobCardStatus.READY_FOR_DELIVERY -> Color(0xFF4CAF50)
        JobCardStatus.COMPLETED -> Color(0xFF757575)
        JobCardStatus.CANCELLED -> Color(0xFFF44336)
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small,
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
