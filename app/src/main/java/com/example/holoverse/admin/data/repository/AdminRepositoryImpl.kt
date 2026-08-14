package com.example.holoverse.admin.data.repository

import com.example.holoverse.admin.domain.repository.AdminRepository
import com.example.holoverse.admin.domain.repository.Timeframe
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.core.utils.NetworkConstant.COLLECTION_NAME_ADMINS
import com.example.holoverse.core.utils.NetworkConstant.COLLECTION_NAME_MENTORS
import com.example.holoverse.core.utils.NetworkConstant.COLLECTION_NAME_STUDENTS
import com.example.holoverse.core.utils.NetworkConstant.COLLECTION_NAME_TRANSACTIONS
import com.example.holoverse.core.utils.Response
import com.example.holoverse.payment.domain.model.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class AdminRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AdminRepository {

    override fun getStudentCount(): Flow<Response<Int>> = flow {
        emit(Response.Loading)
        try {
            val count = firestore.collection(COLLECTION_NAME_STUDENTS)
                .count()
                .get(AggregateSource.SERVER)
                .await()
                .count.toInt()
            emit(Response.Success(count))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.message ?: "Failed to get student count"))
        }
    }

    override fun getMentorCount(): Flow<Response<Int>> = flow {
        emit(Response.Loading)
        try {
            val count = firestore.collection(COLLECTION_NAME_MENTORS)
                .count()
                .get(AggregateSource.SERVER)
                .await()
                .count.toInt()
            emit(Response.Success(count))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.message ?: "Failed to get mentor count"))
        }
    }

    override fun getCourseCount(): Flow<Response<Int>> = flow {
        emit(Response.Loading)
        try {
            val count = firestore.collection("courses")
                .count()
                .get(AggregateSource.SERVER)
                .await()
                .count.toInt()
            emit(Response.Success(count))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.message ?: "Failed to get course count"))
        }
    }

    override fun getCoursesByCategory(): Flow<Response<Map<String, Int>>> = flow {
        emit(Response.Loading)
        try {
            val snapshot = firestore.collection("courses").get().await()
            val distribution = snapshot.documents.groupBy { 
                it.getString("category") ?: "Other" 
            }.mapValues { it.value.size }
            emit(Response.Success(distribution))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.message ?: "Failed to get category distribution"))
        }
    }

    override fun getUserGrowthData(timeframe: Timeframe): Flow<Response<List<Pair<String, Int>>>> = flow {
        emit(Response.Loading)
        try {
            val now = System.currentTimeMillis()
            val startTime = when (timeframe) {
                Timeframe.DAY -> now - 24 * 60 * 60 * 1000L
                Timeframe.WEEK -> now - 7 * 24 * 60 * 60 * 1000L
                Timeframe.MONTH -> now - 30 * 24 * 60 * 60 * 1000L
                Timeframe.YEAR -> now - 365 * 24 * 60 * 60 * 1000L
            }

            val studentQuery = firestore.collection(COLLECTION_NAME_STUDENTS)
                .whereGreaterThanOrEqualTo("createdAt", startTime)
                .get().await()
            
            val mentorQuery = firestore.collection(COLLECTION_NAME_MENTORS)
                .whereGreaterThanOrEqualTo("createdAt", startTime)
                .get().await()

            val allTimestamps = studentQuery.mapNotNull { it.getLong("createdAt") } +
                                mentorQuery.mapNotNull { it.getLong("createdAt") }

            val dateFormat = when (timeframe) {
                Timeframe.DAY -> "HH:00"
                Timeframe.WEEK, Timeframe.MONTH -> "MM/dd"
                Timeframe.YEAR -> "MMM"
            }
            val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())

            val grouped = allTimestamps.groupBy { sdf.format(Date(it)) }
                .mapValues { it.value.size }
                .toList()
                .sortedBy { it.first } // Simple sort, for true temporal sort might need more logic

            emit(Response.Success(grouped))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.message ?: "Failed to get growth data"))
        }
    }

    override fun getTotalRevenue(): Flow<Response<Double>> = flow {
        emit(Response.Loading)
        try {
            val adminsSnapshot = firestore.collection(COLLECTION_NAME_ADMINS).get().await()
            val adminIds = adminsSnapshot.documents.map { it.id }
            
            val transactionsSnapshot = firestore.collection(COLLECTION_NAME_TRANSACTIONS).get().await()
            val totalRevenue = transactionsSnapshot.toObjects(Transaction::class.java)
                .filter { adminIds.contains(it.receiverId) }
                .sumOf { it.amount }
                
            emit(Response.Success(totalRevenue))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.message ?: "Failed to calculate revenue"))
        }
    }

    override fun getRecentTransactions(): Flow<Response<List<Transaction>>> = flow {
        emit(Response.Loading)
        try {
            val snapshot = firestore.collection(COLLECTION_NAME_TRANSACTIONS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()
            val transactions = snapshot.toObjects(Transaction::class.java)
            emit(Response.Success(transactions))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.message ?: "Failed to fetch recent transactions"))
        }
    }

    override fun getAllUsers(): Flow<Response<List<User>>> = flow {
        emit(Response.Loading)
        try {
            val studentDocs = firestore.collection(COLLECTION_NAME_STUDENTS).get().await()
            val mentorDocs = firestore.collection(COLLECTION_NAME_MENTORS).get().await()

            val students = studentDocs.mapNotNull { doc ->
                doc.toObject(User.Student::class.java).copy(userId = doc.id)
            }
            val mentors = mentorDocs.mapNotNull { doc ->
                doc.toObject(User.Mentor::class.java).copy(userId = doc.id)
            }

            emit(Response.Success(students + mentors))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.message ?: "Failed to fetch users"))
        }
    }
}

