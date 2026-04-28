package com.example.garageapp.feature.jobcard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateJobCardScreen(
    customerId: String,
    customerName: String,
    customerPhone: String,
    vehicleId: String,
    vehicleNumber: String,
    onBack: () -> Unit,
    onJobCardCreated: () -> Unit,
    viewModel: CreateJobCardViewModel = hiltViewModel()
) {
    var complaint by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    val isSaving by viewModel.isSaving.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedLabelColor = Color(0xFF1A237E),
        unfocusedLabelColor = Color.Gray,
        focusedBorderColor = Color(0xFF1A237E),
        unfocusedBorderColor = Color.LightGray,
        cursorColor = Color(0xFF1A237E)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Create Job Card", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("New Entry", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A237E),
                    navigationIconContentColor = Color(0xFF1A237E)
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F7FA))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Vehicle & Customer Info Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Vehicle: $vehicleNumber", fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                    Text("Customer: $customerName", fontSize = 14.sp)
                    Text("Phone: $customerPhone", fontSize = 14.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Job Details", 
                modifier = Modifier.align(Alignment.Start),
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = complaint,
                onValueChange = { complaint = it },
                label = { Text("Customer Complaint / Issues") },
                placeholder = { Text("e.g. Engine noise, Brake service") },
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Inspection Notes / Observations") },
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    if (complaint.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Please enter customer complaint") }
                    } else {
                        viewModel.createJobCard(
                            customerId = customerId,
                            customerName = customerName,
                            customerPhone = customerPhone,
                            vehicleId = vehicleId,
                            vehicleNumber = vehicleNumber,
                            complaint = complaint,
                            notes = notes,
                            onSuccess = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Job Card created successfully")
                                    onJobCardCreated()
                                }
                            },
                            onError = { error ->
                                scope.launch { snackbarHostState.showSnackbar("Error: $error") }
                            }
                        )
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Create Job Card", fontSize = 18.sp, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}
