package com.example.holoverse.di

import android.content.Context
import androidx.room.Room
import com.example.holoverse.search.data.local.SearchDatabase
import com.example.holoverse.search.data.local.dao.RecentSearchDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SearchModule {

    @Provides
    @Singleton
    fun provideSearchDatabase(@ApplicationContext context: Context): SearchDatabase {
        return Room.databaseBuilder(
            context,
            SearchDatabase::class.java,
            SearchDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideRecentSearchDao(database: SearchDatabase): RecentSearchDao {
        return database.recentSearchDao
    }
}
