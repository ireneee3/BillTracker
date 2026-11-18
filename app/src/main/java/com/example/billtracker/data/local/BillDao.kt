package com.example.billtracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Query("SELECT * FROM bills ORDER BY id DESC")
    fun getAllBills(): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE id = :id")
    fun getBillById(id: Int): Flow<Bill?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bill: Bill)

    @Update
    suspend fun update(bill: Bill)

    @Delete
    suspend fun delete(bill: Bill)

    @Query("SELECT * FROM bills WHERE isPaid = 0")
    suspend fun getUnpaidBills(): List<Bill>
}
