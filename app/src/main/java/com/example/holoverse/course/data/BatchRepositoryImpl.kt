package com.example.holoverse.course.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.holoverse.core.utils.Response
import com.example.holoverse.course.domain.Batch
import com.example.holoverse.course.domain.BatchSession
import com.example.holoverse.course.domain.BatchStatus
import com.example.holoverse.course.domain.SessionStatus
import com.example.holoverse.course.domain.repository.BatchRepository
import com.example.holoverse.notifications.domain.repository.NotificationRepository
import com.example.holoverse.notifications.presentation.LessonReminderReceiver
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BatchRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val notificationRepository: NotificationRepository,
    @ApplicationContext private val context: Context
) : BatchRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override fun createBatch(batch: Batch): Flow<Response<String>> = flow {
        emit(Response.Loading)
        try {
            val batchId = if (batch.id.isEmpty()) firestore.collection("batches").document().id else batch.id
            val finalBatch = batch.copy(id = batchId)
            firestore.collection("batches").document(batchId).set(finalBatch).await()
            emit(Response.Success(batchId))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.message ?: "Failed to create batch"))
        }
    }

    override fun getBatchesForCourse(courseId: String): Flow<Response<List<Batch>>> = callbackFlow {
        trySend(Response.Loading)
        val listener = firestore.collection("batches")
            .whereEqualTo("courseId", courseId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Response.Error(error.message ?: "Error fetching batches"))
                    return@addSnapshotListener
                }
                val batches = snapshot?.toObjects(Batch::class.java) ?: emptyList()
                trySend(Response.Success(batches))
            }
        awaitClose { listener.remove() }
    }

    override fun joinBatch(batchId: String, userId: String): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            firestore.runTransaction { transaction ->
                val batchRef = firestore.collection("batches").document(batchId)
                val snapshot = transaction.get(batchRef)
                val batch = snapshot.toObject(Batch::class.java) ?: throw Exception("Batch not found")
                
                if (batch.enrolledStudentIds.size >= batch.capacity) {
                    throw Exception("Batch is full")
                }
                
                if (batch.enrolledStudentIds.contains(userId)) {
                    return@runTransaction // Already joined
                }
                
                val newStudents = batch.enrolledStudentIds.toMutableList().apply { add(userId) }
                val newStatus = if (newStudents.size >= batch.capacity) BatchStatus.ACTIVE else batch.status
                
                transaction.update(batchRef, "enrolledStudentIds", newStudents)
                transaction.update(batchRef, "status", newStatus)
            }.await()
            emit(Response.Success(true))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.message ?: "Failed to join batch"))
        }
    }

    override fun joinOrCreateBatch(
        courseId: String,
        mentorId: String,
        userId: String,
        timeSlot: String
    ): Flow<Response<String>> = flow {
        emit(Response.Loading)
        try {
            // Re-implementing with a more robust logic for joinOrCreate
            val batchesQuery = firestore.collection("batches")
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("timeSlot", timeSlot)
                .whereEqualTo("status", BatchStatus.WAITING.name)
                .get()
                .await()

            val availableBatch = batchesQuery.documents.mapNotNull { it.toObject(Batch::class.java) }
                .find { it.enrolledStudentIds.size < it.capacity }

            if (availableBatch != null) {
                // Join existing
                firestore.runTransaction { transaction ->
                    val ref = firestore.collection("batches").document(availableBatch.id)
                    val snapshot = transaction.get(ref)
                    val currentBatch = snapshot.toObject(Batch::class.java)!!
                    
                    if (currentBatch.enrolledStudentIds.size < currentBatch.capacity) {
                        val newStudents = currentBatch.enrolledStudentIds.toMutableList().apply { add(userId) }
                        transaction.update(ref, "enrolledStudentIds", newStudents)
                        if (newStudents.size >= currentBatch.capacity) {
                            transaction.update(ref, "status", BatchStatus.ACTIVE.name)
                        }
                    } else {
                        throw Exception("Batch filled up during transaction")
                    }
                }.await()
                emit(Response.Success(availableBatch.id))
            } else {
                // Create new
                val newBatchId = firestore.collection("batches").document().id
                val newBatch = Batch(
                    id = newBatchId,
                    courseId = courseId,
                    mentorId = mentorId,
                    timeSlot = timeSlot,
                    enrolledStudentIds = listOf(userId)
                )
                firestore.collection("batches").document(newBatchId).set(newBatch).await()
                
                // Notify Mentor
                repositoryScope.launch {
                    try {
                        val courseDoc = firestore.collection("courses").document(courseId).get().await()
                        val courseName = courseDoc.getString("name") ?: "Course"
                        notificationRepository.sendMentorBatchNotification(mentorId, courseName, timeSlot)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                emit(Response.Success(newBatchId))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.message ?: "Failed to join or create batch"))
        }
    }

    override fun proposeSessionTimes(sessionId: String, times: List<Timestamp>): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            firestore.collection("batch_sessions").document(sessionId)
                .update("proposedTimes", times, "status", SessionStatus.PROPOSED.name)
                .await()
            emit(Response.Success(true))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.message ?: "Failed to propose times"))
        }
    }

    override fun voteForSessionTime(sessionId: String, userId: String, timeIndex: Int): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            firestore.runTransaction { transaction ->
                val sessionRef = firestore.collection("batch_sessions").document(sessionId)
                val snapshot = transaction.get(sessionRef)
                val session = snapshot.toObject(BatchSession::class.java) ?: throw Exception("Session not found")
                
                val newVotes = session.votes.toMutableMap()
                // Remove user from all other votes for this session
                newVotes.keys.forEach { key ->
                    val voters = newVotes[key]?.toMutableList() ?: mutableListOf()
                    if (voters.contains(userId)) {
                        voters.remove(userId)
                        newVotes[key] = voters
                    }
                }
                
                // Add to new vote
                val key = timeIndex.toString()
                val targetVoters = newVotes[key]?.toMutableList() ?: mutableListOf()
                targetVoters.add(userId)
                newVotes[key] = targetVoters
                
                transaction.update(sessionRef, "votes", newVotes)
            }.await()
            emit(Response.Success(true))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.message ?: "Failed to vote"))
        }
    }

    override fun finalizeSessionTime(sessionId: String, selectedTime: Timestamp): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            firestore.collection("batch_sessions").document(sessionId)
                .update(
                    "startTime", selectedTime,
                    "status", SessionStatus.CONFIRMED.name
                ).await()

            // Schedule Reminder
            repositoryScope.launch {
                try {
                    val sessionSnapshot = firestore.collection("batch_sessions").document(sessionId).get().await()
                    val batchId = sessionSnapshot.getString("batchId") ?: return@launch
                    val batchDoc = firestore.collection("batches").document(batchId).get().await()
                    val courseId = batchDoc.getString("courseId") ?: return@launch
                    val courseDoc = firestore.collection("courses").document(courseId).get().await()
                    val courseName = courseDoc.getString("name") ?: "Course"

                    scheduleLessonReminder(courseName, selectedTime.toDate().time)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            emit(Response.Success(true))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.message ?: "Failed to finalize session"))
        }
    }

    private fun scheduleLessonReminder(courseName: String, startTimeMillis: Long) {
        val reminderTime = startTimeMillis - (15 * 60 * 1000) // 15 minutes before
        
        if (reminderTime <= System.currentTimeMillis()) return // Already past

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, LessonReminderReceiver::class.java).apply {
            putExtra(LessonReminderReceiver.EXTRA_COURSE_NAME, courseName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            startTimeMillis.toInt(), // Unique ID per session
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Fallback for Android 12+ if permission not granted
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        }
    }

    override fun getBatchSessions(batchId: String): Flow<Response<List<BatchSession>>> = callbackFlow {
        trySend(Response.Loading)
        val listener = firestore.collection("batch_sessions")
            .whereEqualTo("batchId", batchId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Response.Error(error.message ?: "Error fetching sessions"))
                    return@addSnapshotListener
                }
                val sessions = snapshot?.toObjects(BatchSession::class.java) ?: emptyList()
                trySend(Response.Success(sessions))
            }
        awaitClose { listener.remove() }
    }

    override fun getSessionById(sessionId: String): Flow<Response<BatchSession?>> = callbackFlow {
        trySend(Response.Loading)
        val listener = firestore.collection("batch_sessions").document(sessionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Response.Error(error.message ?: "Error fetching session"))
                    return@addSnapshotListener
                }
                val session = snapshot?.toObject(BatchSession::class.java)
                trySend(Response.Success(session))
            }
        awaitClose { listener.remove() }
    }
}
