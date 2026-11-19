package com.example.billtracker

import android.app.Application
import com.example.billtracker.data.local.AppDatabase
import com.example.billtracker.data.repository.BillRepository
import com.example.billtracker.data.repository.PaymentRepository
import com.example.billtracker.reminders.NotificationHelper
import com.example.billtracker.reminders.ReminderScheduler

class BillTrackerApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val billRepository by lazy { BillRepository(database.billDao()) }
    val paymentRepository by lazy { PaymentRepository(database.paymentDao()) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)   // notification channel (Android 8+)
        ReminderScheduler.scheduleDaily(this)    // daily background check via WorkManager
        //ReminderScheduler.runNow(this)
    }
}