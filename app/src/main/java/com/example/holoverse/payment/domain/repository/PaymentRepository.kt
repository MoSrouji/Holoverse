package com.example.holoverse.payment.domain.repository

import com.example.holoverse.core.utils.Response
import com.example.holoverse.payment.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    suspend fun transferFunds(
        senderId: String,
        receiverId: String,
        amount: Double,
        type: com.example.holoverse.payment.domain.model.TransactionType,
        metadata: Map<String, String> = emptyMap()
    ): Response<Boolean>

    fun getTransactionsForUser(userId: String): Flow<Response<List<Transaction>>>
    fun getAllTransactions(): Flow<Response<List<Transaction>>>
    fun getAdminRevenue(): Flow<Response<Double>>
}
