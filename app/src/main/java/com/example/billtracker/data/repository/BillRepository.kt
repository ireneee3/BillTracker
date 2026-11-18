package com.example.billtracker.data.repository

import com.example.billtracker.data.local.Bill
import com.example.billtracker.data.local.BillDao
import kotlinx.coroutines.flow.Flow

class BillRepository(private val billDao: BillDao) {
    val allBills: Flow<List<Bill>> = billDao.getAllBills()

    fun getBill(id: Int): Flow<Bill?> = billDao.getBillById(id)

    suspend fun addBill(bill: Bill) = billDao.insert(bill)
    suspend fun updateBill(bill: Bill) = billDao.update(bill)
    suspend fun deleteBill(bill: Bill) = billDao.delete(bill)
    suspend fun getUnpaidBills(): List<Bill> = billDao.getUnpaidBills()
}
