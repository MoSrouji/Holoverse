package com.example.holoverse.payment.data.repository

import com.example.holoverse.core.utils.NetworkConstant.COLLECTION_NAME_ADMINS
import com.example.holoverse.core.utils.NetworkConstant.COLLECTION_NAME_MENTORS
import com.example.holoverse.core.utils.NetworkConstant.COLLECTION_NAME_STUDENTS
import com.example.holoverse.core.utils.NetworkConstant.COLLECTION_NAME_TRANSACTIONS
import com.example.holoverse.core.utils.Response
import com.example.holoverse.payment.domain.model.Transaction
import com.example.holoverse.payment.domain.model.TransactionType
import com.example.holoverse.payment.domain.repository.PaymentRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PaymentRepository {

    override suspend fun transferFunds(
        senderId: String,
        receiverId: String,
        amount: Double,
        type: TransactionType,
        metadata: Map<String, String>
    ): Response<Boolean> {
        return try {
            val senderRef = findUserRefAsync(senderId) ?: throw Exception("Sender not found")
            val receiverRef = findUserRefAsync(receiverId) ?: throw Exception("Receiver not found")
            
            firestore.runTransaction { transaction ->
                val senderDoc = transaction.get(senderRef)
                val receiverDoc = transaction.get(receiverRef)

                val senderBalance = senderDoc.getDouble("walletBalance") ?: 0.0
                val receiverBalance = receiverDoc.getDouble("walletBalance") ?: 0.0
                
                transaction.update(senderRef, "walletBalance", senderBalance - amount)
                transaction.update(receiverRef, "walletBalance", receiverBalance + amount)

                val transactionId = UUID.randomUUID().toString()
                val paymentTransaction = Transaction(
                    id = transactionId,
                    senderId = senderId,
                    receiverId = receiverId,
                    amount = amount,
                    type = type,
                    timestamp = System.currentTimeMillis(),
                    metadata = metadata
                )

                val transactionRef = firestore.collection(COLLECTION_NAME_TRANSACTIONS).document(transactionId)
                transaction.set(transactionRef, paymentTransaction)
            }.await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Transaction failed")
        }
    }

    private suspend fun findUserRefAsync(userId: String): com.google.firebase.firestore.DocumentReference? {
        val studentDoc = firestore.collection(COLLECTION_NAME_STUDENTS).document(userId).get().await()
        if (studentDoc.exists()) return studentDoc.reference
        
        val mentorDoc = firestore.collection(COLLECTION_NAME_MENTORS).document(userId).get().await()
        if (mentorDoc.exists()) return mentorDoc.reference
        
        val adminDoc = firestore.collection(COLLECTION_NAME_ADMINS).document(userId).get().await()
        if (adminDoc.exists()) return adminDoc.reference
        
        return null
    }

    override fun getTransactionsForUser(userId: String): Flow<Response<List<Transaction>>> = callbackFlow {
        // Fetch where user is either sender or receiver
        // Note: Firestore doesn't support OR on different fields. 
        // We'll combine results or just fetch all and filter for now if it's not too many.
        // Better: use two listeners.
        
        val senderQuery = firestore.collection(COLLECTION_NAME_TRANSACTIONS).whereEqualTo("senderId", userId)
        val receiverQuery = firestore.collection(COLLECTION_NAME_TRANSACTIONS).whereEqualTo("receiverId", userId)

        val senderListener = senderQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Response.Error(error.message ?: "Unknown error"))
                return@addSnapshotListener
            }
            // Logic to merge would be here. For prototype, just fetch all.
        }

        // For simplicity in the prototype, let's fetch all transactions and filter
        val allListener = firestore.collection(COLLECTION_NAME_TRANSACTIONS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Response.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val transactions = snapshot.toObjects(Transaction::class.java)
                        .filter { it.senderId == userId || it.receiverId == userId }
                    trySend(Response.Success(transactions))
                }
            }

        awaitClose { 
            senderListener.remove()
            allListener.remove()
        }
    }

    override fun getAllTransactions(): Flow<Response<List<Transaction>>> = callbackFlow {
        val subscription = firestore.collection(COLLECTION_NAME_TRANSACTIONS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Response.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val transactions = snapshot.toObjects(Transaction::class.java)
                    trySend(Response.Success(transactions))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getAdminRevenue(): Flow<Response<Double>> = flow {
        emit(Response.Loading)
        try {
            // Correct logic: sum all transactions where receiver is an admin
            val adminsSnapshot = firestore.collection(COLLECTION_NAME_ADMINS).get().await()
            val adminIds = adminsSnapshot.documents.map { it.id }
            
            val transactionsSnapshot = firestore.collection(COLLECTION_NAME_TRANSACTIONS).get().await()
            val total = transactionsSnapshot.toObjects(Transaction::class.java)
                .filter { adminIds.contains(it.receiverId) }
                .sumOf { it.amount }
            
            emit(Response.Success(total))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.message ?: "Failed to get revenue"))
        }
    }
}
