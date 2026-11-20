package com.example.billtracker.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billtracker.data.local.Bill
import com.example.billtracker.ui.theme.PurplePrimary
import com.example.billtracker.ui.theme.RedError
import com.example.billtracker.ui.theme.WhiteBackground
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBillScreen(
    existingBill: Bill?,
    bills: List<Bill>,
    onSave: suspend (Bill) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Form State (Pre-filled if editing)
    var billName by remember(existingBill?.id) { mutableStateOf(existingBill?.name ?: "") }
    var amount by remember(existingBill?.id) { mutableStateOf(existingBill?.amount?.toString() ?: "") }
    var category by remember(existingBill?.id) { mutableStateOf(existingBill?.category ?: "") }
    var dueDate by remember(existingBill?.id) { mutableStateOf(existingBill?.dueDate ?: "") }

    // Validation State
    var errorMessage by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = WhiteBackground) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (existingBill == null) "Add New Bill" else "Edit Bill",
                style = MaterialTheme.typography.headlineMedium,
                color = PurplePrimary,
                fontWeight = FontWeight.Bold
            )

            // --- BILL NAME INPUT ---
            OutlinedTextField(
                value = billName,
                onValueChange = { billName = it; errorMessage = "" },
                label = { Text("Bill Name") },
                placeholder = { Text("e.g. Electricity") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurplePrimary,
                    focusedLabelColor = PurplePrimary
                )
            )

            // --- AMOUNT INPUT ---
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it; errorMessage = "" },
                label = { Text("Amount (KSh)") },
                placeholder = { Text("e.g. 2500") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurplePrimary,
                    focusedLabelColor = PurplePrimary
                )
            )

            // --- CATEGORY INPUT ---
            OutlinedTextField(
                value = category,
                onValueChange = { category = it; errorMessage = "" },
                label = { Text("Category") },
                placeholder = { Text("e.g. Utilities, Rent") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurplePrimary,
                    focusedLabelColor = PurplePrimary
                )
            )

            // --- DUE DATE INPUT ---
            OutlinedTextField(
                value = dueDate,
                onValueChange = { dueDate = it; errorMessage = "" },
                label = { Text("Due Date (YYYY-MM-DD)") },
                placeholder = { Text("e.g. 2026-12-31") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurplePrimary,
                    focusedLabelColor = PurplePrimary
                )
            )

            Text(
                text = "Use the date format YYYY-MM-DD.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            // --- ERROR MESSAGE DISPLAY (RED) ---
            if (errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage,
                    color = RedError,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- SAVE BUTTON ---
            Button(
                onClick = {
                    errorMessage = "" // Reset error

                    // 1. Validation: Empty Fields
                    val cleanName = billName.trim()
                    val cleanCategory = category.trim()
                    val cleanAmount = amount.trim().toDoubleOrNull()
                    val cleanDate = dueDate.trim()

                    if (cleanName.isBlank() || cleanCategory.isBlank() || cleanAmount == null || cleanDate.isBlank()) {
                        errorMessage = "Please fill in all fields correctly."
                        return@Button
                    }

                    // 2. Validation: Amount is zero or negative
                    if (cleanAmount <= 0.0) {
                        errorMessage = "Amount must be greater than zero."
                        return@Button
                    }

                    // 3. Validation: Date Format and Past Date Check
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
                    val parsedDate = try {
                        dateFormat.parse(cleanDate)
                    } catch (_: Exception) {
                        null
                    }

                    if (parsedDate == null || dateFormat.format(parsedDate) != cleanDate) {
                        errorMessage = "Enter a valid date in YYYY-MM-DD format."
                        return@Button
                    }

                    val today = dateFormat.format(Date())
                    if (cleanDate < today) {
                        errorMessage = "The due date cannot be in the past."
                        return@Button
                    }

                    // 4. Validation: Duplicate Bill Name
                    val duplicate = bills.any { bill ->
                        bill.name.equals(cleanName, ignoreCase = true) && bill.id != (existingBill?.id ?: 0)
                    }
                    if (duplicate) {
                        errorMessage = "A bill with this name already exists."
                        return@Button
                    }

                    // 5. If all validations pass, Save to Room
                    val billToSave = Bill(
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
                            onSave(billToSave) // This calls the Repository from MainActivity

                            // SHOW SUCCESS MESSAGE
                            Toast.makeText(context, "Bill saved", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Unable to save the bill."
                        } finally {
                            isSaving = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (existingBill == null) "Save Bill" else "Update Bill",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }

            // --- CANCEL BUTTON ---
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PurplePrimary)
            ) {
                Text("Cancel")
            }
        }
    }
}
