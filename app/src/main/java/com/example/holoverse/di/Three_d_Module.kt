package com.example.holoverse.di

import android.content.Context
import com.example.holoverse.three_d_model.data.local.ModelCacheManager
import com.example.holoverse.three_d_model.data.remote.ApiService
import com.example.holoverse.three_d_model.data.repository.ModelRepositoryImpl
import com.example.holoverse.three_d_model.domain.repository.ModelRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Three_d_Module {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            })
            .connectTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(okHttpClient: OkHttpClient): ApiService {
        return Retrofit.Builder()
            .baseUrl(ApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideModelRepository(
        apiService: ApiService
    ): ModelRepository {
        return ModelRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun provideModelCacheManager(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): ModelCacheManager {
        return ModelCacheManager(context, okHttpClient)
    }
}
