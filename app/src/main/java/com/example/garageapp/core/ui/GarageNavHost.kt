package com.example.garageapp.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.garageapp.feature.auth.ui.AuthViewModel
import com.example.garageapp.feature.auth.ui.LoginScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.garageapp.feature.auth.ui.AuthUiState
import com.example.garageapp.feature.dashboard.ui.DashboardScreen
import com.example.garageapp.feature.customer.ui.CustomerListScreen
import com.example.garageapp.feature.customer.ui.AddCustomerScreen
import com.example.garageapp.feature.vehicle.ui.AddVehicleScreen
import com.example.garageapp.feature.jobcard.ui.CreateJobCardScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Customers : Screen("customers")
    object AddCustomer : Screen("add_customer")
    object AddVehicle : Screen("add_vehicle/{customerId}/{customerName}") {
        fun createRoute(customerId: String, customerName: String) = "add_vehicle/$customerId/$customerName"
    }
    object CreateJobCard : Screen("create_job_card/{customerId}/{customerName}/{customerPhone}/{vehicleId}/{vehicleNumber}") {
        fun createRoute(customerId: String, customerName: String, customerPhone: String, vehicleId: String, vehicleNumber: String) =
            "create_job_card/$customerId/$customerName/$customerPhone/$vehicleId/$vehicleNumber"
    }
    object Vehicles : Screen("vehicles")
    object JobCards : Screen("job_cards")
    object Invoices : Screen("invoices")
}

@Composable
fun GarageNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            if (uiState is AuthUiState.Success) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
            
            LoginScreen(
                uiState = uiState,
                onLogin = { email, password -> viewModel.signIn(email, password) },
                isLoading = uiState is AuthUiState.Loading,
                errorMessage = (uiState as? AuthUiState.Error)?.message
            )
        }
        
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onCustomersClick = { navController.navigate(Screen.Customers.route) },
                onVehiclesClick = { navController.navigate(Screen.Vehicles.route) },
                onJobCardsClick = { navController.navigate(Screen.JobCards.route) },
                onInvoicesClick = { navController.navigate(Screen.Invoices.route) }
            )
        }
        
        composable(Screen.Customers.route) {
            val viewModel: com.example.garageapp.feature.customer.ui.CustomerListViewModel = hiltViewModel()
            CustomerListScreen(
                onAddCustomer = { navController.navigate(Screen.AddCustomer.route) },
                onCustomerClick = { customerId -> 
                    val customer = viewModel.customers.value.find { it.customerId == customerId }
                    customer?.let {
                        navController.navigate(Screen.AddVehicle.createRoute(it.customerId, it.name))
                    }
                },
                viewModel = viewModel
            )
        }
        
        composable(Screen.AddCustomer.route) {
            AddCustomerScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddVehicle.route,
            arguments = listOf(
                navArgument("customerId") { type = NavType.StringType },
                navArgument("customerName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
            val customerName = backStackEntry.arguments?.getString("customerName") ?: ""
            
            // We need customer phone to create a job card later, 
            // so in a real flow we'd fetch the full customer or pass phone as well.
            // For now, let's assume we fetch it in the next screen or pass it.
            
            AddVehicleScreen(
                customerId = customerId,
                customerName = customerName,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CreateJobCard.route,
            arguments = listOf(
                navArgument("customerId") { type = NavType.StringType },
                navArgument("customerName") { type = NavType.StringType },
                navArgument("customerPhone") { type = NavType.StringType },
                navArgument("vehicleId") { type = NavType.StringType },
                navArgument("vehicleNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
            val customerName = backStackEntry.arguments?.getString("customerName") ?: ""
            val customerPhone = backStackEntry.arguments?.getString("customerPhone") ?: ""
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
            val vehicleNumber = backStackEntry.arguments?.getString("vehicleNumber") ?: ""
            
            CreateJobCardScreen(
                customerId = customerId,
                customerName = customerName,
                customerPhone = customerPhone,
                vehicleId = vehicleId,
                vehicleNumber = vehicleNumber,
                onBack = { navController.popBackStack() },
                onJobCardCreated = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Vehicles.route) {
            Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                androidx.compose.material3.Text("Vehicle List Coming Soon")
            }
        }
        composable(Screen.JobCards.route) {
            Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                androidx.compose.material3.Text("Job Cards Coming Soon")
            }
        }
        composable(Screen.Invoices.route) {
            Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                androidx.compose.material3.Text("Invoices Coming Soon")
            }
        }
    }
}
