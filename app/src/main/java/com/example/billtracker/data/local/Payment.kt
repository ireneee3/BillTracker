package com.example.billtracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val billId: Int = 0,
    val amount: Double = 0.0,
    val paymentDate: String = ""
)
