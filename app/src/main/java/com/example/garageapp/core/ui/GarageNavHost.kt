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
import com.example.garageapp.feature.vehicle.ui.VehicleListScreen
import com.example.garageapp.feature.vehicle.ui.VehicleSearchScreen
import com.example.garageapp.feature.jobcard.ui.CreateJobCardScreen
import com.example.garageapp.feature.jobcard.ui.JobCardListScreen
import com.example.garageapp.feature.jobcard.ui.JobCardDetailsScreen
import com.example.garageapp.feature.invoice.ui.CreateInvoiceScreen
import com.example.garageapp.feature.invoice.ui.InvoiceListScreen
import com.example.garageapp.feature.invoice.ui.InvoiceDetailsScreen
import com.example.garageapp.feature.settings.ui.WorkshopSettingsScreen
import com.example.garageapp.feature.report.ui.ReportScreen
import com.example.garageapp.feature.payment.ui.PaymentEntryScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Customers : Screen("customers")
    object AddCustomer : Screen("add_customer?customerId={customerId}") {
        fun createRoute(customerId: String? = null) = if (customerId != null) "add_customer?customerId=$customerId" else "add_customer"
    }
    object CustomerVehicles : Screen("customer_vehicles/{customerId}/{customerName}") {
        fun createRoute(customerId: String, customerName: String) = "customer_vehicles/$customerId/$customerName"
    }
    object AddVehicle : Screen("add_vehicle/{customerId}/{customerName}") {
        fun createRoute(customerId: String, customerName: String) = "add_vehicle/$customerId/$customerName"
    }
    object CreateJobCard : Screen("create_job_card/{customerId}/{customerName}/{customerPhone}/{vehicleId}/{vehicleNumber}") {
        fun createRoute(customerId: String, customerName: String, customerPhone: String, vehicleId: String, vehicleNumber: String) =
            "create_job_card/$customerId/$customerName/$customerPhone/$vehicleId/$vehicleNumber"
    }
    object Vehicles : Screen("vehicles")
    object JobCards : Screen("job_cards")
    object JobCardDetails : Screen("job_card_details/{jobCardId}") {
        fun createRoute(jobCardId: String) = "job_card_details/$jobCardId"
    }
    object Invoices : Screen("invoices")
    object CreateInvoice : Screen("create_invoice/{jobCardId}") {
        fun createRoute(jobCardId: String) = "create_invoice/$jobCardId"
    }
    object InvoiceDetails : Screen("invoice_details/{invoiceId}") {
        fun createRoute(invoiceId: String) = "invoice_details/$invoiceId"
    }
    object Settings : Screen("settings")
    object Reports : Screen("reports")
    object AddPayment : Screen("add_payment/{invoiceId}/{invoiceNumber}/{balanceAmount}") {
        fun createRoute(invoiceId: String, invoiceNumber: String, balanceAmount: Double) =
            "add_payment/$invoiceId/$invoiceNumber/$balanceAmount"
    }
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
                onInvoicesClick = { navController.navigate(Screen.Invoices.route) },
                onReportsClick = { navController.navigate(Screen.Reports.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }
        
        composable(Screen.Customers.route) {
            val viewModel: com.example.garageapp.feature.customer.ui.CustomerListViewModel = hiltViewModel()
            CustomerListScreen(
                onAddCustomer = { navController.navigate(Screen.AddCustomer.createRoute()) },
                onEditCustomer = { customerId -> navController.navigate(Screen.AddCustomer.createRoute(customerId)) },
                onCustomerClick = { customerId -> 
                    val customer = viewModel.customers.value.find { it.customerId == customerId }
                    customer?.let {
                        navController.navigate(Screen.CustomerVehicles.createRoute(it.customerId, it.name))
                    }
                },
                viewModel = viewModel
            )
        }
        
        composable(
            route = Screen.AddCustomer.route,
            arguments = listOf(navArgument("customerId") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId")
            AddCustomerScreen(
                customerId = customerId,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CustomerVehicles.route,
            arguments = listOf(
                navArgument("customerId") { type = NavType.StringType },
                navArgument("customerName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
            val customerName = backStackEntry.arguments?.getString("customerName") ?: ""
            val viewModel: com.example.garageapp.feature.customer.ui.CustomerListViewModel = hiltViewModel()
            val customer = viewModel.customers.value.find { it.customerId == customerId }

            VehicleListScreen(
                customerId = customerId,
                customerName = customerName,
                onBack = { navController.popBackStack() },
                onAddVehicle = { navController.navigate(Screen.AddVehicle.createRoute(customerId, customerName)) },
                onStartJobCard = { vehicle ->
                    navController.navigate(
                        Screen.CreateJobCard.createRoute(
                            customerId = customerId,
                            customerName = customerName,
                            customerPhone = customer?.phoneNumber ?: "",
                            vehicleId = vehicle.vehicleId,
                            vehicleNumber = vehicle.vehicleNumber
                        )
                    )
                }
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
            VehicleSearchScreen(
                onBack = { navController.popBackStack() },
                onStartJobCard = { vehicle ->
                    navController.navigate(Screen.CustomerVehicles.createRoute(vehicle.customerId, "Customer"))
                }
            )
        }

        composable(Screen.JobCards.route) {
            JobCardListScreen(
                onBack = { navController.popBackStack() },
                onJobCardClick = { jobCardId ->
                    navController.navigate(Screen.JobCardDetails.createRoute(jobCardId))
                }
            )
        }

        composable(
            route = Screen.JobCardDetails.route,
            arguments = listOf(navArgument("jobCardId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobCardId = backStackEntry.arguments?.getString("jobCardId") ?: ""
            JobCardDetailsScreen(
                jobCardId = jobCardId,
                onBack = { navController.popBackStack() },
                onCreateInvoice = { navController.navigate(Screen.CreateInvoice.createRoute(jobCardId)) }
            )
        }

        composable(Screen.Invoices.route) {
            InvoiceListScreen(
                onBack = { navController.popBackStack() },
                onInvoiceClick = { invoiceId ->
                    navController.navigate(Screen.InvoiceDetails.createRoute(invoiceId))
                }
            )
        }

        composable(
            route = Screen.CreateInvoice.route,
            arguments = listOf(navArgument("jobCardId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobCardId = backStackEntry.arguments?.getString("jobCardId") ?: ""
            CreateInvoiceScreen(
                jobCardId = jobCardId,
                onBack = { navController.popBackStack() },
                onInvoiceCreated = { invoiceId ->
                    navController.navigate(Screen.InvoiceDetails.createRoute(invoiceId)) {
                        popUpTo(Screen.CreateInvoice.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.InvoiceDetails.route,
            arguments = listOf(navArgument("invoiceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getString("invoiceId") ?: ""
            InvoiceDetailsScreen(
                invoiceId = invoiceId,
                onBack = { navController.popBackStack() },
                onAddPayment = { invId, invNum, balance ->
                    navController.navigate(Screen.AddPayment.createRoute(invId, invNum, balance))
                }
            )
        }

        composable(
            route = Screen.AddPayment.route,
            arguments = listOf(
                navArgument("invoiceId") { type = NavType.StringType },
                navArgument("invoiceNumber") { type = NavType.StringType },
                navArgument("balanceAmount") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getString("invoiceId") ?: ""
            val invoiceNumber = backStackEntry.arguments?.getString("invoiceNumber") ?: ""
            val balanceAmount = backStackEntry.arguments?.getFloat("balanceAmount")?.toDouble() ?: 0.0
            
            PaymentEntryScreen(
                invoiceId = invoiceId,
                invoiceNumber = invoiceNumber,
                balanceAmount = balanceAmount,
                onBack = { navController.popBackStack() },
                onPaymentAdded = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            WorkshopSettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Reports.route) {
            ReportScreen(onBack = { navController.popBackStack() })
        }
    }
}
