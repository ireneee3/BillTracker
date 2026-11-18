package com.example.billtracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val amount: Double = 0.0,
    val dueDate: String = "",
    val isPaid: Boolean = false
)
