package com.example.billtracker.reminders

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Pure date helpers for the reminder feature (no Android classes, so they are unit-testable).
 * Bill.dueDate is stored as text in the format yyyy-MM-dd.
 */
object ReminderDates {

    private const val DATE_PATTERN = "yyyy-MM-dd"
    private const val DAY_MS = 24L * 60 * 60 * 1000

    /** Midnight at the start of the day that contains [millis]. */
    fun startOfDay(millis: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    /** Turns "2026-11-21" into milliseconds, or null if the text is not a valid yyyy-MM-dd date. */
    fun parseDueDate(dueDate: String): Long? {
        val text = dueDate.trim()
        val format = SimpleDateFormat(DATE_PATTERN, Locale.US).apply { isLenient = false }
        val position = ParsePosition(0)
        val date = format.parse(text, position)
        return if (date == null || position.index != text.length) null else date.time
    }

    /**
     * Whole calendar days from "now" to the due date.
     * 0 = today, 1 = tomorrow, negative = overdue, null = due date could not be read.
     */
    fun daysUntil(nowMillis: Long, dueDate: String): Int? {
        val due = parseDueDate(dueDate) ?: return null
        return Math.round((startOfDay(due) - startOfDay(nowMillis)) / DAY_MS.toDouble()).toInt()
    }
}
