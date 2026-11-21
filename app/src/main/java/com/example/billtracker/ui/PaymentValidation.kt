package com.example.billtracker.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Pure functions (no Android types) so they are easy to unit test. Each returns an error message, or null if valid. */
object PaymentValidation {

    // Must match the format AddEditBillScreen uses for dueDate / paymentDate strings.
    const val DATE_PATTERN = "yyyy-MM-dd"

    fun validateAmount(input: String?): String? {
        val text = input?.trim().orEmpty()
        if (text.isEmpty()) return "Enter the amount paid"
        val value = text.toDoubleOrNull() ?: return "Enter a valid number"
        if (value.isNaN() || value.isInfinite()) return "Enter a valid number"
        if (value <= 0.0) return "Amount must be greater than zero"
        return null
    }

    fun validateDate(input: String?): String? {
        val text = input?.trim().orEmpty()
        if (text.isEmpty()) return "Enter the date paid"
        val fmt = SimpleDateFormat(DATE_PATTERN, Locale.US).apply { isLenient = false }
        return try {
            fmt.parse(text)
            null
        } catch (e: Exception) {
            "Use the format $DATE_PATTERN (e.g. 2026-11-21)"
        }
    }

    fun today(): String = SimpleDateFormat(DATE_PATTERN, Locale.US).format(Date())
}