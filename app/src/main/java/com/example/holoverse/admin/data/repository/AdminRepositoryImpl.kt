package com.example.holoverse.admin.data.repository

import android.util.Log
import com.example.holoverse.admin.domain.repository.AdminRepository
import com.example.holoverse.admin.domain.repository.Timeframe
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.core.utils.DummyDataPopulator
import com.example.holoverse.core.utils.NetworkConstant.COLLECTION_NAME_USERS
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
            val count = firestore.collection(COLLECTION_NAME_USERS)
                .whereEqualTo("accountType", "Student")
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
            val count = firestore.collection(COLLECTION_NAME_USERS)
                .whereEqualTo("accountType", "Mentor")
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

            val userGrowthQuery = firestore.collection(COLLECTION_NAME_USERS)
                .whereGreaterThanOrEqualTo("createdAt", startTime)
                .get().await()

            val allTimestamps = userGrowthQuery.mapNotNull { it.getLong("createdAt") }

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
            val adminsSnapshot = firestore.collection(COLLECTION_NAME_USERS)
                .whereEqualTo("accountType", "Admin")
                .get().await()
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
            val userDocs = firestore.collection(COLLECTION_NAME_USERS).get().await()

            val users = userDocs.mapNotNull { doc ->
                val typeString = doc.getString("accountType")
                when (typeString) {
                    "Student" -> doc.toObject(User.Student::class.java).copy(userId = doc.id)
                    "Mentor" -> doc.toObject(User.Mentor::class.java).copy(userId = doc.id)
                    "Admin" -> doc.toObject(User.Admin::class.java).copy(userId = doc.id)
                    else -> null
                }
            }

            emit(Response.Success(users))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.message ?: "Failed to fetch users"))
        }
    }

    override suspend fun runMigration(): Response<Boolean> {
        return try {
            Log.d("Migration", "Starting migration to unified structure...")
            
            // 1. Migrate Students
            val students = firestore.collection("students").get().await()
            Log.d("Migration", "Migrating ${students.size()} students...")
            students.documents.chunked(500).forEach { chunk ->
                firestore.runBatch { batch ->
                    chunk.forEach { doc ->
                        val data = doc.data?.toMutableMap() ?: mutableMapOf()
                        data["accountType"] = "Student"
                        batch.set(firestore.collection(COLLECTION_NAME_USERS).document(doc.id), data)
                    }
                }.await()
            }

            // 2. Migrate Teachers
            val teachers = firestore.collection("teachers").get().await()
            Log.d("Migration", "Migrating ${teachers.size()} teachers...")
            teachers.documents.chunked(500).forEach { chunk ->
                firestore.runBatch { batch ->
                    chunk.forEach { doc ->
                        val data = doc.data?.toMutableMap() ?: mutableMapOf()
                        data["accountType"] = "Mentor"
                        batch.set(firestore.collection(COLLECTION_NAME_USERS).document(doc.id), data)
                    }
                }.await()
            }

            // 3. Migrate Admins
            val admins = firestore.collection("admins").get().await()
            Log.d("Migration", "Migrating ${admins.size()} admins...")
            admins.documents.chunked(500).forEach { chunk ->
                firestore.runBatch { batch ->
                    chunk.forEach { doc ->
                        val data = doc.data?.toMutableMap() ?: mutableMapOf()
                        data["accountType"] = "Admin"
                        batch.set(firestore.collection(COLLECTION_NAME_USERS).document(doc.id), data)
                    }
                }.await()
            }

            // 4. Migrate Boosted Courses
            val boosted = firestore.collection("boostedCourses").get().await()
            Log.d("Migration", "Migrating ${boosted.size()} boosted courses...")
            boosted.documents.chunked(500).forEach { chunk ->
                firestore.runBatch { batch ->
                    chunk.forEach { doc ->
                        val courseId = doc.getString("courseId") ?: doc.id
                        val updateData = mapOf(
                            "isBoosted" to true,
                            "boostExpiry" to (doc.getLong("endTimestamp") ?: 0L),
                            "adCardStyle" to (doc.getString("adCardStyle") ?: "STYLE_1")
                        )
                        batch.update(firestore.collection("courses").document(courseId), updateData)
                    }
                }.await()
            }

            Log.d("Migration", "Migration completed successfully!")
            Response.Success(true)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("Migration", "Migration failed: ${e.message}", e)
            Response.Error(e.message ?: "Migration failed")
        }
    }

    override suspend fun populateDummyData(): Response<Boolean> {
        return try {
            Log.d("AdminRepo", "Resetting and populating dummy data...")
            
            // 1. Clear Existing Courses
            val courses = firestore.collection("courses").get().await()
            courses.documents.chunked(500).forEach { chunk ->
                firestore.runBatch { batch ->
                    chunk.forEach { batch.delete(it.reference) }
                }.await()
            }

            // 2. Clear Dummy Mentors (ones starting with mentor_dummy_)
            val users = firestore.collection(COLLECTION_NAME_USERS).get().await()
            users.documents.filter { it.id.startsWith("mentor_dummy_") }.chunked(500).forEach { chunk ->
                firestore.runBatch { batch ->
                    chunk.forEach { batch.delete(it.reference) }
                }.await()
            }

            // 3. Populate
            DummyDataPopulator(firestore).populateData()
            
            Response.Success(true)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("AdminRepo", "Population failed: ${e.message}", e)
            Response.Error(e.message ?: "Data population failed")
        }
    }
}

