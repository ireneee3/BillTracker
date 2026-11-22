package com.example.billtracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.billtracker.data.local.Bill
import com.example.billtracker.data.local.Payment

@Composable
fun PaymentHistoryScreen(
    bill: Bill?,
    payments: List<Payment>,
    onBack: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Payment History",
            style = MaterialTheme.typography.headlineSmall
        )
        if (bill != null) {
            Text(bill.name, style = MaterialTheme.typography.titleMedium)
        }

        if (payments.isEmpty()) {
            Text("No payments recorded yet.")
        } else {
            // Newest first
            payments.sortedByDescending { it.paymentDate }.forEach { payment ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(payment.paymentDate)
                        Text(
                            text = "KSh ${"%.2f".format(payment.amount)}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Bill")
        }
    }
}