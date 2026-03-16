package com.example.holoverse.courses.data

import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.utils.Response
import kotlinx.coroutines.flow.Flow

interface CourseRepo {
    suspend fun addCourse(course: Courses): Flow<Response<Boolean>>
    suspend fun deleteCourse(course: Courses): Flow<Response<Boolean>>
    suspend fun updateCourse(course: Courses): Flow<Response<Boolean>>
    suspend fun getCourseById(courseId: String): Flow<Response<Courses?>>
    suspend fun getCoursesByCategory(category: String): Flow<Response<List<Courses>>>
}
