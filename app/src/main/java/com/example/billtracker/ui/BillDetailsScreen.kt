package com.example.billtracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.billtracker.data.local.Bill

@Composable
fun BillDetailsScreen(
    bill: Bill,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    onMarkPaid: () -> Unit,
    onPaymentHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Bill Details",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = bill.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Amount: KSh ${"%.2f".format(bill.amount)}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = "Category: ${bill.category}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Due Date: ${bill.dueDate}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = if (bill.isPaid) "Status: PAID" else "Status: UNPAID",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (bill.isPaid) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                )
            }
        }

        if (!bill.isPaid) {
            Button(
                onClick = onMarkPaid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mark as Paid")
            }
        }

        OutlinedButton(
            onClick = onPaymentHistory,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Payment History")
        }

        Button(
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Edit Bill")
        }

        Button(
            onClick = onDelete,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
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