package com.example.billtracker.reminders

import android.content.Context
import androidx.core.content.edit

/**
 * Stores the reminder settings. The Settings screen (Member 6) WRITES these values and
 * ReminderWorker READS them every time it runs, so a change takes effect without rescheduling.
 */
class ReminderPreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    /** How many days before the due date the user wants to be reminded. */
    var daysBefore: Int
        get() = prefs.getInt(KEY_DAYS_BEFORE, DEFAULT_DAYS_BEFORE)
        set(value) = prefs.edit { putInt(KEY_DAYS_BEFORE, value.coerceAtLeast(0)) }

    var remindersEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_ENABLED, value) }

    companion object {
        private const val FILE_NAME = "bill_tracker_prefs"
        private const val KEY_DAYS_BEFORE = "reminder_days_before"
        private const val KEY_ENABLED = "reminders_enabled"
        const val DEFAULT_DAYS_BEFORE = 2
    }
}
