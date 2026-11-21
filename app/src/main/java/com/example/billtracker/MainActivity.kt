
package com.example.billtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.billtracker.data.local.Bill
import com.example.billtracker.reminders.NotificationPermissionRequester
import com.example.billtracker.ui.theme.BillTrackerTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.billtracker.ui.AddEditBillScreen
import com.example.billtracker.ui.BillDetailsScreen

class MainActivity : ComponentActivity() {

    // Must be created as a property (registered before the activity starts)
    private val notificationPermission = NotificationPermissionRequester(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationPermission.requestIfNeeded()   // asks for POST_NOTIFICATIONS on Android 13+

        val app = application as BillTrackerApp

        enableEdgeToEdge()

        setContent {
            BillTrackerTheme {
                BillTrackerNavigation(
                    app = app
                )
            }
        }
    }
}

@Composable
fun BillTrackerNavigation(app: BillTrackerApp) {

    val bills by app.billRepository.allBills.collectAsState(
        initial = emptyList()
    )

    var currentScreen by remember { mutableStateOf("Dashboard") }
    var selectedBillId by remember { mutableIntStateOf(0) }
    var billToDelete by remember { mutableStateOf<Bill?>(null) }

    val selectedBill = bills.find { it.id == selectedBillId }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Financial Bill Tracker",
                style = MaterialTheme.typography.headlineMedium
            )

            when (currentScreen) {

                "Dashboard" -> {
                    DashboardScreen(
                        bills = bills,
                        onViewBills = {
                            currentScreen = "Bills"
                        },
                        onAddBill = {
                            selectedBillId = 0
                            currentScreen = "Add Bill"
                        },
                        onSettings = {
                            currentScreen = "Settings"
                        }
                    )
                }

                "Bills" -> {
                    BillsScreen(
                        bills = bills,
                        onAddBill = {
                            selectedBillId = 0
                            currentScreen = "Add Bill"
                        },
                        onSelectBill = { bill ->
                            selectedBillId = bill.id
                            currentScreen = "Bill Details"
                        },
                        onBack = {
                            currentScreen = "Dashboard"
                        }
                    )
                }

                "Add Bill" -> {
                    AddEditBillScreen(
                        existingBill = null,
                        bills = bills,
                        onSave = { bill ->
                            app.billRepository.addBill(bill)
                            currentScreen = "Bills"
                        },
                        onBack = {
                            currentScreen = "Bills"
                        }
                    )
                }

                "Edit Bill" -> {
                    if (selectedBill != null) {
                        AddEditBillScreen(
                            existingBill = selectedBill,
                            bills = bills,
                            onSave = { updatedBill ->
                                app.billRepository.updateBill(updatedBill)
                                currentScreen = "Bill Details"
                            },
                            onBack = {
                                currentScreen = "Bill Details"
                            }
                        )
                    } else {
                        Text("Bill not found.")
                        Button(onClick = {
                            currentScreen = "Bills"
                        }) {
                            Text("Back to Bills")
                        }
                    }
                }

                "Bill Details" -> {
                    if (selectedBill != null) {
                        BillDetailsScreen(
                            bill = selectedBill,
                            onEdit = {
                                currentScreen = "Edit Bill"
                            },
                            onDelete = {
                                billToDelete = selectedBill
                            },
                            onBack = {
                                currentScreen = "Bills"
                            }
                        )
                    } else {
                        Text("This bill could not be found.")
                        Button(onClick = {
                            currentScreen = "Bills"
                        }) {
                            Text("Back to Bills")
                        }
                    }
                }

                "Payment History" -> {
                    Text(
                        text = "Payment History",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text("Payment history will be displayed here.")

                    OutlinedButton(onClick = {
                        currentScreen = "Bills"
                    }) {
                        Text("Back to Bills")
                    }
                }

                "Settings" -> {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text("Financial Bill Tracker Settings")

                    OutlinedButton(onClick = {
                        currentScreen = "Dashboard"
                    }) {
                        Text("Back to Dashboard")
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    billToDelete?.let { bill ->

        AlertDialog(
            onDismissRequest = {
                billToDelete = null
            },
            title = {
                Text("Delete Bill")
            },
            text = {
                Text("Are you sure you want to delete ${bill.name}? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            app.billRepository.deleteBill(bill)
                            billToDelete = null
                            currentScreen = "Bills"
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        billToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DashboardScreen(
    bills: List<Bill>,
    onViewBills: () -> Unit,
    onAddBill: () -> Unit,
    onSettings: () -> Unit
) {

    val totalBills = bills.size
    val paidBills = bills.count { it.isPaid }
    val unpaidBills = bills.count { !it.isPaid }
    val totalUnpaidAmount = bills
        .filter { !it.isPaid }
        .sumOf { it.amount }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineSmall
        )

        DashboardCard("Total Bills", totalBills.toString())
        DashboardCard("Paid Bills", paidBills.toString())
        DashboardCard("Unpaid Bills", unpaidBills.toString())
        DashboardCard(
            "Outstanding Amount",
            "KSh ${"%.2f".format(totalUnpaidAmount)}"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onAddBill,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add New Bill")
        }

        Button(
            onClick = onViewBills,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View My Bills")
        }

        OutlinedButton(
            onClick = onSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Settings")
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
fun BillsScreen(
    bills: List<Bill>,
    onAddBill: () -> Unit,
    onSelectBill: (Bill) -> Unit,
    onBack: () -> Unit
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "My Bills",
            style = MaterialTheme.typography.headlineSmall
        )

        Button(
            onClick = onAddBill,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add New Bill")
        }

        if (bills.isEmpty()) {

            Text("You have not added any bills yet.")

        } else {

            bills.forEach { bill ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 3.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {

                        Text(
                            text = bill.name,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text("Amount: KSh ${bill.amount}")
                        Text("Category: ${bill.category}")
                        Text("Due date: ${bill.dueDate}")

                        Text(
                            text = if (bill.isPaid) "PAID" else "UNPAID",
                            color = if (bill.isPaid) {
                                Color(0xFF2E7D32)
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )

                        OutlinedButton(
                            onClick = {
                                onSelectBill(bill)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View Details")
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Dashboard")
        }
    }
}

