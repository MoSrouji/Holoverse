package com.example.holoverse.search.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.holoverse.search.data.local.dao.RecentSearchDao
import com.example.holoverse.search.data.local.entities.RecentSearchEntity

@Database(entities = [RecentSearchEntity::class], version = 1, exportSchema = false)
abstract class SearchDatabase : RoomDatabase() {
    abstract val recentSearchDao: RecentSearchDao

    companion object {
        const val DATABASE_NAME = "holoverse_search_db"
    }
}
