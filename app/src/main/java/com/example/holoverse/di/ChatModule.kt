package com.example.holoverse.di

import android.content.Context
import androidx.room.Room
import com.example.holoverse.auth.domain.repositiory.AuthRepository
import com.example.holoverse.chat_system.data.local.ChatDatabase
import com.example.holoverse.chat_system.data.local.dao.ChatDao
import com.example.holoverse.chat_system.data.local.dao.MessageDao
import com.example.holoverse.chat_system.data.remote.FcmApi
import com.example.holoverse.chat_system.data.repository.ChatRepositoryImpl
import com.example.holoverse.chat_system.domain.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatModule {

    @Provides
    @Singleton
    fun provideChatDatabase(@ApplicationContext context: Context): ChatDatabase {
        return Room.databaseBuilder(
            context,
            ChatDatabase::class.java,
            ChatDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideChatDao(database: ChatDatabase): ChatDao = database.chatDao

    @Provides
    @Singleton
    fun provideMessageDao(database: ChatDatabase): MessageDao = database.messageDao

    @Provides
    @Singleton
    fun provideFcmApi(): FcmApi {
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(FcmApi::class.java)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        firestore: FirebaseFirestore,
        chatDao: ChatDao,
        messageDao: MessageDao,
        authRepository: AuthRepository,
        fcmApi: FcmApi,
        @ApplicationContext context: Context
    ): ChatRepository {
        return ChatRepositoryImpl(firestore, chatDao, messageDao, authRepository, fcmApi, context)
    }
}
