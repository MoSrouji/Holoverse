package com.example.holoverse.di

import com.example.holoverse.material.data.repository.MaterialRepositoryImpl
import com.example.holoverse.material.domain.repository.MaterialRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MaterialModule {

    @Provides
    @Singleton
    fun provideMaterialRepository(
        firestore: FirebaseFirestore
    ): MaterialRepository {
        return MaterialRepositoryImpl(firestore)
    }
}
