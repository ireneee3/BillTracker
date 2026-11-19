package com.example.billtracker.reminders

import com.example.billtracker.data.local.Bill

/** One notification that should be shown. */
data class BillReminder(val billId: Int, val message: String)

/**
 * Decides WHICH bills need a reminder today. Kept separate from the Worker so the rule
 * ("unpaid AND due within N days, or overdue") can be unit tested without Android.
 */
object ReminderPlanner {

    fun plan(bills: List<Bill>, nowMillis: Long, daysBefore: Int): List<BillReminder> =
        bills
            .filter { !it.isPaid }                       // never remind for paid bills
            .mapNotNull { bill ->
                val daysLeft = ReminderDates.daysUntil(nowMillis, bill.dueDate)
                    ?: return@mapNotNull null            // skip bills with an unreadable date
                if (daysLeft <= daysBefore) {            // due soon, today, or already overdue
                    BillReminder(bill.id, ReminderMessages.build(bill.name, daysLeft))
                } else {
                    null
                }
            }
}
