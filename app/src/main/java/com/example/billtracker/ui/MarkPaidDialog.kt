package com.example.billtracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.billtracker.data.local.Bill

@Composable
fun MarkPaidDialog(
    bill: Bill,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, date: String) -> Unit
) {
    var amountText by remember { mutableStateOf(bill.amount.toString()) }
    var dateText by remember { mutableStateOf(PaymentValidation.today()) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mark ${bill.name} as paid") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; amountError = null },
                    label = { Text("Amount paid (KSh)") },
                    singleLine = true,
                    isError = amountError != null,
                    supportingText = { amountError?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it; dateError = null },
                    label = { Text("Date paid (${PaymentValidation.DATE_PATTERN})") },
                    singleLine = true,
                    isError = dateError != null,
                    supportingText = { dateError?.let { Text(it) } }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                amountError = PaymentValidation.validateAmount(amountText)
                dateError = PaymentValidation.validateDate(dateText)
                if (amountError == null && dateError == null) {
                    onConfirm(amountText.trim().toDouble(), dateText.trim())
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
