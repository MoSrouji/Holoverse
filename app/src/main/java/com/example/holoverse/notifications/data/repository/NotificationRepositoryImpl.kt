package com.example.holoverse.notifications.data.repository

import android.content.Context
import android.util.Log
import com.example.holoverse.R
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.chat.data.remote.AndroidConfig
import com.example.holoverse.chat.data.remote.AndroidNotification
import com.example.holoverse.chat.data.remote.FcmApi
import com.example.holoverse.chat.data.remote.FcmMessage
import com.example.holoverse.chat.data.remote.FcmV1Request
import com.example.holoverse.chat.data.remote.NotificationData
import com.example.holoverse.notifications.domain.models.Notification
import com.example.holoverse.notifications.domain.repository.NotificationRepository
import com.example.holoverse.core.utils.NetworkConstant.COLLECTION_NAME_MENTORS
import com.example.holoverse.core.utils.Response
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val fcmApi: FcmApi,
    @ApplicationContext private val context: Context
) : NotificationRepository {

    override suspend fun sendCourseNotificationToFollowers(
        mentorId: String,
        mentorName: String,
        courseId: String,
        courseName: String
    ): Response<Boolean> {
        return try {
            // 1. Get followers and mentor info from mentor document
            val mentorDoc = firestore.collection(COLLECTION_NAME_MENTORS).document(mentorId).get().await()
            val followers = mentorDoc.get("followers") as? List<String> ?: emptyList()
            val mentorImageUrl = mentorDoc.getString("profileImageUrl")

            if (followers.isEmpty()) return Response.Success(true)

            val title = context.getString(R.string.new_course_notification_title, mentorName)
            val body = context.getString(R.string.new_course_notification_body, courseName)

            // 2. Create notification for each follower in Firestore
            firestore.runBatch { batch ->
                followers.forEach { followerId ->
                    val notificationRef = firestore.collection("notifications").document()
                    val notification = Notification(
                        id = notificationRef.id,
                        recipientId = followerId,
                        title = title,
                        body = body,
                        type = "course_created",
                        courseId = courseId,
                        timestamp = Timestamp.now(),
                        isRead = false,
                        senderName = mentorName,
                        senderImageUrl = mentorImageUrl
                    )
                    batch.set(notificationRef, notification)
                }
            }.await()

            // 3. Send FCM notifications to each follower
            try {
                val authHeader = getAccessToken()
                Log.d("NotificationRepo", "Sending FCM to ${followers.size} followers")
                followers.forEach { followerId ->
                    val token = authRepository.getFcmToken(followerId)
                    if (!token.isNullOrBlank()) {
                        Log.d("NotificationRepo", "Sending FCM to token: $token")
                        val request = FcmV1Request(
                            message = FcmMessage(
                                token = token,
                                notification = NotificationData(title, body),
                                data = mapOf(
                                    "type" to "course_created",
                                    "courseId" to courseId,
                                    "title" to title,
                                    "body" to body
                                ),
                                android = AndroidConfig(
                                    priority = "high",
                                    notification = AndroidNotification(
                                        channel_id = "general_notifications",
                                        notification_priority = "PRIORITY_HIGH"
                                    )
                                )
                            )
                        )
                        fcmApi.sendNotification(authHeader, request)
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationRepo", "Failed to send FCM notifications: ${e.message}")
            }

            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Failed to send notifications")
        }
    }

    private suspend fun getAccessToken(): String {
        return withContext(Dispatchers.IO) {
            try {
                val stream = context.assets.open("service-account.json")
                val credentials = GoogleCredentials.fromStream(stream)
                    .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
                credentials.refreshIfExpired()
                "Bearer ${credentials.accessToken.tokenValue}"
            } catch (e: Exception) {
                Log.e("NotificationRepo", "Error getting access token: ${e.message}")
                throw e
            }
        }
    }

    override fun getNotifications(userId: String): Flow<Response<List<Notification>>> = callbackFlow {
        trySend(Response.Loading)
        
        // Use a snapshot listener to get real-time updates
        val query = firestore.collection("notifications")
            .whereEqualTo("recipientId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Response.Error(error.message ?: "Unknown error"))
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val notifications = snapshot.toObjects(Notification::class.java)
                // Filter out nulls if any document failed to parse
                trySend(Response.Success(notifications.filterNotNull()))
            }
        }

        awaitClose { listener.remove() }
    }

    override suspend fun markAsRead(notificationId: String): Response<Boolean> {
        return try {
            firestore.collection("notifications").document(notificationId)
                .update("isRead", true).await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Failed to mark notification as read")
        }
    }
}


