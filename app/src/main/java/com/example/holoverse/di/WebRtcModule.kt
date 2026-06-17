package com.example.holoverse.di

import android.content.Context
import com.example.holoverse.webrtc.SignalingClient
import com.example.holoverse.webrtc.WebRtcSessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Module
@InstallIn(SingletonComponent::class)
object WebRtcModule {

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
