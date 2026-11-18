package com.example.billtracker

import android.app.Application
import com.example.billtracker.data.local.AppDatabase
import com.example.billtracker.data.repository.BillRepository
import com.example.billtracker.data.repository.PaymentRepository

class BillTrackerApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val billRepository by lazy { BillRepository(database.billDao()) }
    val paymentRepository by lazy { PaymentRepository(database.paymentDao()) }
}