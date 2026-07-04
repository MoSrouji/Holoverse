package com.example.holoverse.chat_system.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface FcmApi {
    @POST("v1/projects/holoversev1/messages:send")
    suspend fun sendNotification(
        @Header("Authorization") authHeader: String,
        @Body request: FcmV1Request
    )
}

@Serializable
data class FcmV1Request(
    val message: FcmMessage
)

@Serializable
data class FcmMessage(
    val token: String,
    val notification: NotificationData? = null,
    val data: Map<String, String>? = null,
    val android: AndroidConfig? = null
)

@Serializable
data class AndroidConfig(
    val priority: String = "high",
    val notification: AndroidNotification? = null
)

@Serializable
data class AndroidNotification(
    val channel_id: String = "chat_notifications",
    val notification_priority: String = "PRIORITY_HIGH",
    val sound: String = "default",
    val default_vibrate_timings: Boolean = true,
    val default_sound: Boolean = true,
    val visibility: String = "PUBLIC"
)

@Serializable
data class NotificationData(
    val title: String,
    val body: String
)
