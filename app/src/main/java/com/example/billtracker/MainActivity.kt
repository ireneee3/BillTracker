
package com.example.billtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.billtracker.data.local.Bill
import com.example.billtracker.ui.theme.BillTrackerTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
    var selectedBillId by remember { mutableStateOf(0) }
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

@Composable
fun AddEditBillScreen(
    existingBill: Bill?,
    bills: List<Bill>,
    onSave: suspend (Bill) -> Unit,
    onBack: () -> Unit
) {

    var billName by remember(existingBill?.id) {
        mutableStateOf(existingBill?.name ?: "")
    }

    var amount by remember(existingBill?.id) {
        mutableStateOf(existingBill?.amount?.toString() ?: "")
    }

    var category by remember(existingBill?.id) {
        mutableStateOf(existingBill?.category ?: "")
    }

    var dueDate by remember(existingBill?.id) {
        mutableStateOf(existingBill?.dueDate ?: "")
    }

    var errorMessage by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = if (existingBill == null) "Add New Bill" else "Edit Bill",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = billName,
            onValueChange = {
                billName = it
                errorMessage = ""
            },
            label = { Text("Bill Name") },
            placeholder = { Text("e.g. Electricity") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = amount,
            onValueChange = {
                amount = it
                errorMessage = ""
            },
            label = { Text("Amount (KSh)") },
            placeholder = { Text("e.g. 2500") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            singleLine = true
        )

        OutlinedTextField(
            value = category,
            onValueChange = {
                category = it
                errorMessage = ""
            },
            label = { Text("Category") },
            placeholder = { Text("e.g. Utilities, Rent, Internet") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = dueDate,
            onValueChange = {
                dueDate = it
                errorMessage = ""
            },
            label = { Text("Due Date") },
            placeholder = { Text("YYYY-MM-DD") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text(
            text = "Use the date format YYYY-MM-DD.",
            style = MaterialTheme.typography.bodySmall
        )

        if (errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = {

                errorMessage = ""

                val cleanName = billName.trim()
                val cleanCategory = category.trim()
                val cleanAmount = amount.trim().toDoubleOrNull()
                val cleanDate = dueDate.trim()

                if (
                    cleanName.isBlank() ||
                    cleanCategory.isBlank() ||
                    cleanAmount == null ||
                    cleanDate.isBlank()
                ) {
                    errorMessage = "Please fill in all fields correctly."
                    return@Button
                }

                if (cleanAmount <= 0.0) {
                    errorMessage = "Amount must be greater than zero."
                    return@Button
                }

                val dateFormat = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.US
                ).apply {
                    isLenient = false
                }

                val parsedDate = try {
                    dateFormat.parse(cleanDate)
                } catch (e: Exception) {
                    null
                }

                if (
                    parsedDate == null ||
                    dateFormat.format(parsedDate) != cleanDate
                ) {
                    errorMessage = "Enter a valid date in YYYY-MM-DD format."
                    return@Button
                }

                val today = dateFormat.format(Date())

                if (cleanDate < today) {
                    errorMessage = "The due date cannot be in the past."
                    return@Button
                }

                val duplicate = bills.any { bill ->
                    bill.name.equals(cleanName, ignoreCase = true) &&
                            bill.id != (existingBill?.id ?: 0)
                }

                if (duplicate) {
                    errorMessage = "A bill with this name already exists."
                    return@Button
                }

                val bill = Bill(
                    id = existingBill?.id ?: 0,
                    name = cleanName,
                    amount = cleanAmount,
                    dueDate = cleanDate,
                    category = cleanCategory,
                    isPaid = existingBill?.isPaid ?: false
                )

                scope.launch {
                    isSaving = true

                    try {
                        onSave(bill)
                    } catch (e: Exception) {
                        errorMessage = e.message
                            ?: "Unable to save the bill."
                    } finally {
                        isSaving = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            Text(
                if (isSaving) "Saving..." else {
                    if (existingBill == null) "Save Bill" else "Update Bill"
                }
            )
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            Text("Cancel")
        }
    }
}

@Composable
fun BillDetailsScreen(
    bill: Bill,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Bill Details",
            style = MaterialTheme.typography.headlineSmall
        )

        DashboardCard("Bill Name", bill.name)
        DashboardCard("Amount", "KSh ${bill.amount}")
        DashboardCard("Category", bill.category)
        DashboardCard("Due Date", bill.dueDate)
        DashboardCard(
            "Payment Status",
            if (bill.isPaid) "Paid" else "Unpaid"
        )

        Button(
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Edit Bill")
        }

        OutlinedButton(
            onClick = onDelete,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Delete Bill")
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Bills")
        }
    }
}
