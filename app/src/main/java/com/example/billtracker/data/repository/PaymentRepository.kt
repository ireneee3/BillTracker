package com.example.billtracker.data.repository

import com.example.billtracker.data.local.Payment
import com.example.billtracker.data.local.PaymentDao
import kotlinx.coroutines.flow.Flow

class PaymentRepository(private val paymentDao: PaymentDao) {
    fun getPayments(billId: Int): Flow<List<Payment>> = paymentDao.getPaymentsForBill(billId)

    suspend fun addPayment(payment: Payment) = paymentDao.insert(payment)
}
