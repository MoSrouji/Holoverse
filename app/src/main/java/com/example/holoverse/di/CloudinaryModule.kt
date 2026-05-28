package com.example.holoverse.di

import android.content.Context
import com.cloudinary.android.MediaManager
import com.example.holoverse.cloudinary_services.data.repository.CloudinaryRepositoryImpl
import com.example.holoverse.cloudinary_services.domain.repository.CloudinaryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CloudinaryModule {

    @Provides
    @Singleton
    fun provideCloudinaryRepository(
        @ApplicationContext context: Context
    ): CloudinaryRepository {
        return CloudinaryRepositoryImpl(context)
    }
}
