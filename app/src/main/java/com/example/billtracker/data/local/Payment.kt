
package com.example.billtracker.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Bill::class,
            parentColumns = ["id"],
            childColumns = ["billId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["billId"])]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val billId: Int = 0,

    val amount: Double = 0.0,

    val paymentDate: String = ""
)
