
package com.example.billtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.billtracker.ui.theme.BillTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BillTrackerTheme {
                BillTrackerNavigation()
            }
        }
    }
}

@Composable
fun BillTrackerNavigation() {
    var currentScreen by remember { mutableStateOf("Dashboard") }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Financial Bill Tracker",
                style = MaterialTheme.typography.headlineMedium
            )

            when (currentScreen) {

                "Dashboard" -> {
                    Text("Dashboard")

                    Button(onClick = {
                        currentScreen = "Bills"
                    }) {
                        Text("Go to Bills")
                    }

                    Button(onClick = {
                        currentScreen = "Settings"
                    }) {
                        Text("Settings")
                    }
                }

                "Bills" -> {
                    Text("My Bills")

                    Button(onClick = {
                        currentScreen = "Add Bill"
                    }) {
                        Text("Add New Bill")
                    }

                    Button(onClick = {
                        currentScreen = "Bill Details"
                    }) {
                        Text("Open Bill #1 (Test)")
                    }

                    Button(onClick = {
                        currentScreen = "Dashboard"
                    }) {
                        Text("Back to Dashboard")
                    }
                }

                "Add Bill" -> {
                    Text("Add New Bill")

                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("Bill Name") }
                    )

                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("Amount") }
                    )

                    Button(onClick = {
                        currentScreen = "Bills"
                    }) {
                        Text("Save Bill (Demo)")
                    }

                    Button(onClick = {
                        currentScreen = "Bills"
                    }) {
                        Text("Back")
                    }
                }

                "Bill Details" -> {
                    Text("Bill Details")
                    Text("Bill #1 - Test Bill")

                    Button(onClick = {
                        currentScreen = "Edit Bill"
                    }) {
                        Text("Edit Bill")
                    }

                    Button(onClick = {
                        currentScreen = "Payment History"
                    }) {
                        Text("Payment History")
                    }

                    Button(onClick = {
                        currentScreen = "Bills"
                    }) {
                        Text("Back")
                    }
                }

                "Edit Bill" -> {
                    Text("Edit Bill #1")

                    Button(onClick = {
                        currentScreen = "Bill Details"
                    }) {
                        Text("Update Bill (Demo)")
                    }

                    Button(onClick = {
                        currentScreen = "Bill Details"
                    }) {
                        Text("Back")
                    }
                }

                "Payment History" -> {
                    Text("Payment History")
                    Text("No payment records yet.")

                    Button(onClick = {
                        currentScreen = "Bill Details"
                    }) {
                        Text("Back")
                    }
                }

                "Settings" -> {
                    Text("Settings")
                    Text("Financial Bill Tracker Settings")

                    Button(onClick = {
                        currentScreen = "Dashboard"
                    }) {
                        Text("Back")
                    }
                }
            }
        }
    }
}
