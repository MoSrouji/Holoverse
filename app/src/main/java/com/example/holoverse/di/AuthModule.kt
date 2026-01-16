package com.example.holoverse.di

import com.example.holoverse.auth.data.repository.AuthRepositoryImpl
import com.example.holoverse.auth.domain.repositiory.AuthRepository
import com.example.holoverse.auth.domain.use_cases.AuthUseCases
import com.example.holoverse.auth.domain.use_cases.FirebaseSignIn
import com.example.holoverse.auth.domain.use_cases.FirebaseSignUp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent ::class)
object AuthModule {


    @Singleton
    @Provides
    fun provideAuthenticationRepository(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore
    ): AuthRepository {

        return AuthRepositoryImpl(
            firebaseAuth = firebaseAuth,
            firestore = firebaseFirestore
        )
    }

    @Singleton
    @Provides
    fun provideAuthenticationUseCases(
        repository: AuthRepository
    ) = AuthUseCases(
        firebaseSignUp = FirebaseSignUp(repository = repository),
        firebaseSignIn = FirebaseSignIn(repository = repository)
    )
}