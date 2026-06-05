package com.example.holoverse.search.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.holoverse.search.data.local.entities.RecentSearchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentSearchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recentSearch: RecentSearchEntity)

    @Query("SELECT * FROM recent_searches WHERE userId = :userId AND searchType = :searchType ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(userId: String, searchType: String): Flow<List<RecentSearchEntity>>

    @Query("DELETE FROM recent_searches WHERE userId = :userId AND `query` = :query AND searchType = :searchType")
    suspend fun delete(userId: String, query: String, searchType: String)

    @Query("DELETE FROM recent_searches WHERE userId = :userId")
    suspend fun clearAll(userId: String)
}
