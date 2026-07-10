package com.example.holoverse.search.domain.repository

import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.course.domain.Courses
import com.example.holoverse.search.domain.model.CourseFilters
import com.example.holoverse.search.domain.model.MentorFilters
import com.example.holoverse.search.presentation.SearchType
import com.example.holoverse.core.utils.Response
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun searchCourses(filters: CourseFilters): Flow<Response<List<Courses>>>
    fun searchMentors(filters: MentorFilters): Flow<Response<List<User.Mentor>>>

    fun getRecentSearches(userId: String, type: SearchType): Flow<List<String>>
    suspend fun saveRecentSearch(user: User, query: String, type: SearchType)
    suspend fun removeRecentSearch(user: User, query: String, type: SearchType)
}


