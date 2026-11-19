package com.example.billtracker.reminders

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private const val DAILY_WORK_NAME = "daily_bill_reminder"
    private const val TEST_PERIODIC_WORK_NAME = "test_bill_reminder_15min"
    private const val TEST_NOW_WORK_NAME = "test_bill_reminder_now"

    /**
     * Schedules the real reminder: every 24 hours, first run at the next [hourOfDay]:00.
     * It is "unique" work with KEEP, so calling this on every app start does NOT create duplicates.
     */
    fun scheduleDaily(context: Context, hourOfDay: Int = 8) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true) // skip the check while the battery is critically low
            .build()

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(millisUntilNext(hourOfDay), TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancelDaily(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_WORK_NAME)
    }

    // ---------- TESTING HELPERS ----------

    /** Runs the check ONCE, right now. Add an unpaid bill due soon, then call this. */
    fun runNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<ReminderWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            TEST_NOW_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /** Android's minimum repeat interval for periodic work is 15 minutes. Good for testing the repeat. */
    fun scheduleEvery15Minutes(context: Context) {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            TEST_PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request
        )
    }

    private fun millisUntilNext(hourOfDay: Int): Long {
        val now = Calendar.getInstance()
        val next = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        return next.timeInMillis - now.timeInMillis
    }
}
