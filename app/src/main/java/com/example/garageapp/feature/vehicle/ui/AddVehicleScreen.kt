package com.example.garageapp.feature.vehicle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.garageapp.domain.model.Vehicle
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    customerId: String,
    customerName: String,
    onBack: () -> Unit,
    viewModel: AddVehicleViewModel = hiltViewModel()
) {
    var vehicleNumber by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
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
                        Text("Add Vehicle", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("For: $customerName", fontSize = 12.sp, color = Color.Gray)
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = vehicleNumber,
                onValueChange = { vehicleNumber = it.uppercase() },
                label = { Text("Vehicle Number (e.g. ABC-1234)") },
                singleLine = true,
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Characters
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("Vehicle Make / Model") },
                singleLine = true,
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Words
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Additional Notes (Color, Engine No, etc.)") },
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    if (vehicleNumber.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Vehicle number is required") }
                    } else {
                        viewModel.addVehicle(
                            Vehicle(
                                vehicleId = UUID.randomUUID().toString(),
                                customerId = customerId,
                                vehicleNumber = vehicleNumber,
                                model = model,
                                notes = notes,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            ),
                            onSuccess = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Vehicle saved successfully")
                                    onBack()
                                }
                            },
                            onError = { errorMsg ->
                                scope.launch { snackbarHostState.showSnackbar("Error: $errorMsg") }
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
                    Text("Register Vehicle", fontSize = 18.sp, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}
