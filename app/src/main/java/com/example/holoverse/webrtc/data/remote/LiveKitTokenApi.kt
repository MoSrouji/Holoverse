package com.example.holoverse.webrtc.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface LiveKitTokenApi {
    @POST(".")
    suspend fun getToken(
        @Body request: TokenRequest
    ): TokenResponse
}

@Serializable
data class TokenRequest(
    val roomName: String,
    val participantName: String
)

@Serializable
data class TokenResponse(
    val token: String
)
