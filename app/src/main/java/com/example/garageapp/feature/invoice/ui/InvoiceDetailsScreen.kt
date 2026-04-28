package com.example.garageapp.feature.invoice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.garageapp.core.util.PdfGenerator
import com.example.garageapp.core.util.ShareUtils
import com.example.garageapp.domain.model.Invoice
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailsScreen(
    invoiceId: String,
    onBack: () -> Unit,
    onAddPayment: (String, String, Double) -> Unit = { _, _, _ -> },
    viewModel: InvoiceDetailsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val invoice by viewModel.invoice.collectAsState()
    val workshop by viewModel.workshopDetails.collectAsState()
    val items by viewModel.items.collectAsState()

    LaunchedEffect(invoiceId) {
        viewModel.loadInvoice(invoiceId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(invoice?.invoiceNumber ?: "Invoice Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        invoice?.let { inv ->
                            val file = PdfGenerator.generateInvoicePdf(context, inv, items, workshop)
                            file?.let { ShareUtils.shareFile(context, it, inv.customerPhone) }
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (invoice == null) {
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
                InvoiceHeaderCard(invoice!!)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Invoice Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        SummaryRow("Subtotal", "Rs. ${invoice!!.subtotal.toInt()}")
                        SummaryRow("Discount", "Rs. ${invoice!!.discount.toInt()}")
                        SummaryRow("Total Amount", "Rs. ${invoice!!.totalAmount.toInt()}", isBold = true)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        SummaryRow("Paid Amount", "Rs. ${invoice!!.paidAmount.toInt()}", color = Color(0xFF388E3C))
                        SummaryRow("Balance Due", "Rs. ${invoice!!.balanceAmount.toInt()}", isBold = true, color = if (invoice!!.balanceAmount > 0) Color.Red else Color.Black)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (invoice!!.balanceAmount > 0) {
                    Button(
                        onClick = { onAddPayment(invoice!!.invoiceId, invoice!!.invoiceNumber, invoice!!.balanceAmount) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                    ) {
                        Text("Record Payment")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedButton(
                    onClick = {
                        invoice?.let { inv ->
                            val message = "Dear customer, your invoice ${inv.invoiceNumber} for vehicle ${inv.vehicleNumber} is ready. Total amount: Rs. ${inv.totalAmount.toInt()}. Balance: Rs. ${inv.balanceAmount.toInt()}. Thank you."
                            ShareUtils.shareText(context, message, inv.customerPhone)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Share Message via WhatsApp")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        invoice?.let { inv ->
                            val file = PdfGenerator.generateInvoicePdf(context, inv, items, workshop)
                            file?.let { ShareUtils.shareFile(context, it, inv.customerPhone) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate & Share PDF")
                }
            }
        }
    }
}

@Composable
fun InvoiceHeaderCard(invoice: Invoice) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Customer", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(invoice.customerName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(invoice.customerPhone, fontSize = 14.sp)
                }
                PaymentStatusChip(status = invoice.paymentStatus)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Vehicle", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(invoice.vehicleNumber, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Date", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(invoice.createdAt))
                    Text(date)
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isBold: Boolean = false, color: Color = Color.Black) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, color = color)
    }
}
