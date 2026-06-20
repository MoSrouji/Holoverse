package com.example.holoverse.di

import android.content.Context
import com.example.holoverse.webrtc.data.datasource.SignalingClient
import com.example.holoverse.webrtc.data.datasource.WebRtcSessionManager
import com.example.holoverse.webrtc.data.repository.WebRtcRepositoryImpl
import com.example.holoverse.webrtc.domain.repository.WebRtcRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
        fun provideWebRtcSessionManager(
            @ApplicationContext context: Context,
            signalingClient: SignalingClient
        ): WebRtcSessionManager {
            return WebRtcSessionManager(context, signalingClient)
        }
    }
}
