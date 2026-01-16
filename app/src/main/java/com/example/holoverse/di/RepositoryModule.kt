package com.example.holoverse.di

import com.example.holoverse.courses.data.CourseRepo
import com.example.holoverse.courses.data.CourseRepoImpl
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

   

    @Provides
    @Singleton
    fun provideCourseRepo(firestore: FirebaseFirestore): CourseRepo {
        return CourseRepoImpl(firestore)
    }
}
