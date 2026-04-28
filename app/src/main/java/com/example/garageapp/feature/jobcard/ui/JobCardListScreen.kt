package com.example.garageapp.feature.jobcard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary)
                    .statusBarsPadding()
            ) {
                TopAppBar(
                    title = { Text("Service Job Cards", fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search Job No, Name or Vehicle...", color = Color.White.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (filteredJobCards.isEmpty()) {
                Text(
                    text = "No job cards found matching your search",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredJobCards) { jobCard ->
                        JobCardListItem(
                            jobCard = jobCard,
                            onClick = { onJobCardClick(jobCard.jobCardId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JobCardListItem(jobCard: JobCard, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = jobCard.jobCardNumber,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = Color(0xFF1A237E)
                )
                JobCardStatusChip(status = jobCard.status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = jobCard.vehicleNumber, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 15.sp, 
                    color = Color(0xFF212121),
                    modifier = Modifier.background(Color(0xFFF5F5F5), MaterialTheme.shapes.extraSmall).padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = jobCard.customerName, fontSize = 14.sp, color = Color(0xFF616161))
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF5F5F5))
            
            val date = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(jobCard.createdAt))
            Text(text = "Registration Date: $date", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun JobCardStatusChip(status: JobCardStatus) {
    val color = when (status) {
        JobCardStatus.OPEN -> Color(0xFF1976D2)
        JobCardStatus.IN_PROGRESS -> Color(0xFFF57C00)
        JobCardStatus.READY_FOR_DELIVERY -> Color(0xFF388E3C)
        JobCardStatus.COMPLETED -> Color(0xFF455A64)
        JobCardStatus.CANCELLED -> Color(0xFFD32F2F)
    }
    
    Surface(
        color = color.copy(alpha = 0.08f),
        shape = MaterialTheme.shapes.small,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, color)
    ) {
        Text(
            text = status.name.replace("_", " "),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}
