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
    val notification: NotificationData,
    val data: Map<String, String>
)

@Serializable
data class NotificationData(
    val title: String,
    val body: String
)
