package com.example.holoverse.di

import android.content.Context
import com.example.holoverse.admin.data.repository.AdminRepositoryImpl
import com.example.holoverse.admin.domain.repository.AdminRepository
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.chat.data.remote.FcmApi
import com.example.holoverse.course.data.BatchRepositoryImpl
import com.example.holoverse.course.data.CourseRepo
import com.example.holoverse.course.data.CourseRepoImpl
import com.example.holoverse.course.domain.repository.BatchRepository
import com.example.holoverse.notifications.data.repository.NotificationRepositoryImpl
import com.example.holoverse.notifications.domain.repository.NotificationRepository
import com.example.holoverse.payment.data.repository.PaymentRepositoryImpl
import com.example.holoverse.payment.domain.repository.PaymentRepository
import com.example.holoverse.reviews.data.ReviewRepositoryImpl
import com.example.holoverse.reviews.domain.ReviewRepository
import com.example.holoverse.search.data.local.dao.RecentSearchDao
import com.example.holoverse.search.data.repository.SearchRepositoryImpl
import com.example.holoverse.search.domain.repository.SearchRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun provideBatchRepository(
        firestore: FirebaseFirestore,
        notificationRepository: NotificationRepository,
        @ApplicationContext context: Context
    ): BatchRepository {
        return BatchRepositoryImpl(firestore, notificationRepository, context)
    }

    @Provides
    @Singleton
    fun provideSearchRepository(
        firestore: FirebaseFirestore,
        recentSearchDao: RecentSearchDao
    ): SearchRepository {
        return SearchRepositoryImpl(firestore, recentSearchDao)
    }

    @Provides
    @Singleton
    fun provideReviewRepository(firestore: FirebaseFirestore): ReviewRepository {
        return ReviewRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(
        firestore: FirebaseFirestore,
        authRepository: AuthRepository,
        fcmApi: FcmApi,
        @ApplicationContext context: Context
    ): NotificationRepository {
        return NotificationRepositoryImpl(firestore, authRepository, fcmApi, context)
    }

    @Provides
    @Singleton
    fun provideAdminRepository(firestore: FirebaseFirestore): AdminRepository {
        return AdminRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun providePaymentRepository(firestore: FirebaseFirestore): PaymentRepository {
        return PaymentRepositoryImpl(firestore)
    }
}

