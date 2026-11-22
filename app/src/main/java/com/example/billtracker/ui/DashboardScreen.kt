package com.example.billtracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onBillClick: (Int) -> Unit,
    vm: DashboardViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Dashboard") }) }) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is UiState.Loading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                is UiState.Error ->
                    Text(s.message, color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp))

                is UiState.Success -> {
                    val d = s.data
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("This month", style = MaterialTheme.typography.titleMedium)
                        }
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TotalCard("Total due", d.totalDue, Modifier.weight(1f))
                                TotalCard("Paid", d.totalPaid, Modifier.weight(1f))
                                TotalCard("Unpaid", d.totalUnpaid, Modifier.weight(1f))
                            }
                        }
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text("Due soon (next 7 days)", style = MaterialTheme.typography.titleMedium)
                        }
                        if (d.dueSoon.isEmpty()) {
                            item { Text("Nothing due soon. You're all caught up!") }
                        } else {
                            items(d.dueSoon, key = { it.id }) { bill ->
                                BillItem(bill, onClick = { onBillClick(bill.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TotalCard(label: String, amount: Double, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text("KES %.0f".format(amount), style = MaterialTheme.typography.titleMedium)
        }
    }
}
