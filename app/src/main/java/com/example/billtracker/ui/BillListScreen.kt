package com.example.billtracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.billtracker.data.local.Bill
import com.example.billtracker.reminders.ReminderDates
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(dueDate: String): String {
    val millis = ReminderDates.parseDueDate(dueDate) ?: return dueDate
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillListScreen(
    onBillClick: (Int) -> Unit,
    onAddClick: () -> Unit,
    vm: BillListViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Bills") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add bill")
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is UiState.Loading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                is UiState.Error ->
                    Text(
                        "Something went wrong: ${s.message}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp)
                    )

                is UiState.Success ->
                    if (s.data.isEmpty()) {
                        Text(
                            "No bills yet, tap + to add one",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center).padding(24.dp)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(s.data, key = { it.id }) { bill ->
                                BillItem(bill, onClick = { onBillClick(bill.id) })
                            }
                        }
                    }
            }
        }
    }
}

@Composable
fun BillItem(bill: Bill, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(bill.name, style = MaterialTheme.typography.titleMedium)
                Text("${bill.category} • Due ${formatDate(bill.dueDate)}",
                    style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("KES %.2f".format(bill.amount), style = MaterialTheme.typography.titleMedium)
                Text(
                    if (bill.isPaid) "Paid" else "Unpaid",
                    color = if (bill.isPaid) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
