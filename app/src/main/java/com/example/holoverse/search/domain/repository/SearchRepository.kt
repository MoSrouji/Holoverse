package com.example.holoverse.search.domain.repository

import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.search.domain.model.CourseFilters
import com.example.holoverse.search.domain.model.MentorFilters
import com.example.holoverse.utils.Response
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun searchCourses(filters: CourseFilters): Flow<Response<List<Courses>>>
    fun searchMentors(filters: MentorFilters): Flow<Response<List<User.Mentor>>>
}