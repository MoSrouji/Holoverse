package com.example.holoverse.search.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val query: String,
    val searchType: String, // "COURSES" or "MENTORS"
    val timestamp: Long = System.currentTimeMillis()
)
