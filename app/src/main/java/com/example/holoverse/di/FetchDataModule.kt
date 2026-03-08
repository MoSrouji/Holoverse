package com.example.holoverse.di

import com.example.holoverse.fetch.data.FetchDataRepositoryImpl
import com.example.holoverse.fetch.domain.FetchDataRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object FetchDataModule {

    @Provides
    @Singleton
   fun provideFetchDataRepository(
        firestore: FirebaseFirestore
    ): FetchDataRepository{
       return FetchDataRepositoryImpl(firestore)

    }
}