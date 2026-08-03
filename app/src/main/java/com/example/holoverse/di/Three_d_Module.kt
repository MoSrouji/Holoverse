package com.example.holoverse.di

import android.content.Context
import com.example.holoverse.threedmodel.data.local.ModelCacheManager
import com.example.holoverse.threedmodel.data.remote.ApiService
import com.example.holoverse.threedmodel.data.repository.ModelRepositoryImpl
import com.example.holoverse.threedmodel.domain.repository.ModelRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Three_d_Module {

    @Provides
    @Singleton
    fun provideApiService(
        @LongTimeoutClient okHttpClient: OkHttpClient,
        moshi: Moshi
    ): ApiService {
        return Retrofit.Builder()
            .baseUrl(ApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideModelRepository(
        apiService: ApiService,
        firestore: FirebaseFirestore
    ): ModelRepository {
        return ModelRepositoryImpl(apiService, firestore)
    }

    @Provides
    @Singleton
    fun provideModelCacheManager(
        @ApplicationContext context: Context,
        @LongTimeoutClient okHttpClient: OkHttpClient
    ): ModelCacheManager {
        return ModelCacheManager(context, okHttpClient)
    }
}
