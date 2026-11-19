package com.example.billtracker.reminders

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.billtracker.BillTrackerApp
import kotlin.coroutines.cancellation.CancellationException

/**
 * Runs once a day in the background, even if the app is closed.
 *
 *  1. Read the "days before" setting
 *  2. Ask the BillRepository for UNPAID bills (Repository -> DAO -> Room)
 *  3. Let ReminderPlanner pick the ones due within the window (or overdue)
 *  4. Show one notification per bill
 */
class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = ReminderPreferences(applicationContext)
            if (!prefs.remindersEnabled) return Result.success()

            val app = applicationContext as BillTrackerApp
            val unpaidBills = app.billRepository.getUnpaidBills()

            val reminders = ReminderPlanner.plan(
                bills = unpaidBills,
                nowMillis = System.currentTimeMillis(),
                daysBefore = prefs.daysBefore
            )
            reminders.forEach { reminder ->
                NotificationHelper.showBillReminder(
                    applicationContext,
                    reminder.billId,
                    reminder.message
                )
            }

            Log.d(TAG, "Checked ${unpaidBills.size} unpaid bill(s), sent ${reminders.size} reminder(s)")
            Result.success()
        } catch (e: CancellationException) {
            throw e // never swallow coroutine cancellation
        } catch (e: Exception) {
            Log.e(TAG, "Reminder check failed, will retry", e)
            Result.retry() // WorkManager tries again later with backoff
        }
    }

    companion object {
        private const val TAG = "ReminderWorker"
    }
}
