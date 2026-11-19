package com.example.billtracker.reminders

import com.example.billtracker.data.local.Bill
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderLogicTest {

    // "Today" for every test: 19 Nov 2026, 10:00
    private val now = at(2026, Calendar.NOVEMBER, 19, 10, 0)

    // ---- messages ----

    @Test
    fun message_dueInTwoDays() {
        assertEquals("Electricity bill due in 2 days", ReminderMessages.build("Electricity", 2))
    }

    @Test
    fun message_dueTodayAndTomorrow() {
        assertEquals("Rent bill due today", ReminderMessages.build("Rent", 0))
        assertEquals("Internet bill due tomorrow", ReminderMessages.build("Internet", 1))
    }

    @Test
    fun message_overdue() {
        assertEquals("Water bill overdue by 3 days", ReminderMessages.build("Water", -3))
        assertEquals("Water bill overdue by 1 day", ReminderMessages.build("Water", -1))
    }

    // ---- date maths ----

    @Test
    fun daysUntil_countsCalendarDays() {
        assertEquals(0, ReminderDates.daysUntil(now, "2026-11-19"))
        assertEquals(1, ReminderDates.daysUntil(now, "2026-11-20"))
        assertEquals(2, ReminderDates.daysUntil(now, "2026-11-21"))
        assertEquals(-2, ReminderDates.daysUntil(now, "2026-11-17"))
    }

    @Test
    fun daysUntil_lateEveningStillCountsTomorrowAsOneDay() {
        val lateEvening = at(2026, Calendar.NOVEMBER, 19, 23, 45)
        assertEquals(1, ReminderDates.daysUntil(lateEvening, "2026-11-20"))
    }

    @Test
    fun daysUntil_badDateReturnsNull() {
        assertNull(ReminderDates.daysUntil(now, ""))
        assertNull(ReminderDates.daysUntil(now, "21/11/2026"))
        assertNull(ReminderDates.daysUntil(now, "2026-13-45"))
    }

    // ---- which bills get a reminder ----

    @Test
    fun plan_remindsForUnpaidBillDueWithinWindow() {
        val bills = listOf(bill(1, "Electricity", "2026-11-21"))
        val result = ReminderPlanner.plan(bills, now, daysBefore = 2)
        assertEquals(listOf(BillReminder(1, "Electricity bill due in 2 days")), result)
    }

    @Test
    fun plan_neverRemindsForPaidBills() {
        val bills = listOf(bill(1, "Rent", "2026-11-20", isPaid = true))
        assertTrue(ReminderPlanner.plan(bills, now, daysBefore = 2).isEmpty())
    }

    @Test
    fun plan_skipsBillsDueAfterTheWindow() {
        val bills = listOf(bill(1, "Internet", "2026-11-25"))
        assertTrue(ReminderPlanner.plan(bills, now, daysBefore = 2).isEmpty())
    }

    @Test
    fun plan_includesOverdueUnpaidBills() {
        val bills = listOf(bill(1, "Water", "2026-11-16"))
        assertEquals("Water bill overdue by 3 days", ReminderPlanner.plan(bills, now, 2)[0].message)
    }

    @Test
    fun plan_respectsDaysBeforeSetting() {
        val bills = listOf(bill(1, "Rent", "2026-11-22")) // 3 days away
        assertTrue(ReminderPlanner.plan(bills, now, daysBefore = 2).isEmpty())
        assertEquals(1, ReminderPlanner.plan(bills, now, daysBefore = 3).size)
    }

    // ---- helpers ----

    private fun bill(id: Int, name: String, due: String, isPaid: Boolean = false) =
        Bill(id = id, name = name, amount = 1000.0, dueDate = due, category = "Utilities", isPaid = isPaid)

    private fun at(y: Int, m: Int, d: Int, h: Int, min: Int): Long =
        Calendar.getInstance().apply {
            set(y, m, d, h, min, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
}
