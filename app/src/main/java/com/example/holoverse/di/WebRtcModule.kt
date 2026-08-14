package com.example.holoverse.di

import android.content.Context
import com.example.holoverse.webrtc.data.datasource.LiveKitSessionManager
import com.example.holoverse.webrtc.data.datasource.SignalingClient
import com.example.holoverse.webrtc.data.remote.LiveKitTokenApi
import com.example.holoverse.webrtc.data.repository.WebRtcRepositoryImpl
import com.example.holoverse.webrtc.domain.repository.WebRtcRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WebRtcModule {

    @Binds
    @Singleton
    abstract fun bindWebRtcRepository(
        webRtcRepositoryImpl: WebRtcRepositoryImpl
    ): WebRtcRepository

    companion object {
        @Singleton
        @Provides
        fun provideSignalingClient(
            firestore: FirebaseFirestore,
            auth: FirebaseAuth
        ): SignalingClient {
            return SignalingClient(firestore, auth)
        }

        @Singleton
        @Provides
        fun provideLiveKitSessionManager(
            @ApplicationContext context: Context,
            signalingClient: SignalingClient
        ): LiveKitSessionManager {
            return LiveKitSessionManager(context, signalingClient)
        }

        @Singleton
        @Provides
        @Named("TokenRetrofit")
        fun provideTokenRetrofit(okHttpClient: OkHttpClient): Retrofit {
            val json = Json { ignoreUnknownKeys = true }
            val baseUrl = com.example.holoverse.BuildConfig.TOKEN_SERVER_URL
            return Retrofit.Builder()
                .baseUrl(if (baseUrl.isNotEmpty()) baseUrl else "https://placeholder.com/")
                .client(okHttpClient)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
        }

        @Singleton
        @Provides
        fun provideLiveKitTokenApi(@Named("TokenRetrofit") retrofit: Retrofit): LiveKitTokenApi {
            return retrofit.create(LiveKitTokenApi::class.java)
        }
    }
}
