package com.example.billtracker.reminders

/** Builds the text shown in the notification. Pure function, so it is easy to unit test. */
object ReminderMessages {

    const val TITLE = "Bill reminder"

    fun build(billName: String, daysLeft: Int): String = when {
        daysLeft < 0 -> "$billName bill overdue by ${-daysLeft} ${dayWord(-daysLeft)}"
        daysLeft == 0 -> "$billName bill due today"
        daysLeft == 1 -> "$billName bill due tomorrow"
        else -> "$billName bill due in $daysLeft days"
    }

    private fun dayWord(n: Int) = if (n == 1) "day" else "days"
}
