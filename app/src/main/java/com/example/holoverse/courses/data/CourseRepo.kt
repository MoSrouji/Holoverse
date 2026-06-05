package com.example.holoverse.courses.data

import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.courses.domain.BoostedCourse
import com.example.holoverse.utils.Response
import kotlinx.coroutines.flow.Flow

interface CourseRepo {
    suspend fun addCourse(course: Courses): Flow<Response<Boolean>>
    suspend fun deleteCourse(course: Courses): Flow<Response<Boolean>>
    suspend fun updateCourse(course: Courses): Flow<Response<Boolean>>
    suspend fun getCourseById(courseId: String): Flow<Response<Courses?>>
    suspend fun getCoursesByCategory(category: AppCategory): Flow<Response<List<Courses>>>
    suspend fun getCoursesByInstructorId(instructorId: String): Flow<Response<List<Courses>>>

    // Boost methods
    suspend fun boostCourse(boostedCourse: BoostedCourse): Flow<Response<Boolean>>
    suspend fun getBoostedCourses(): Flow<Response<List<BoostedCourse>>>
    suspend fun deleteBoostedCourse(courseId: String): Flow<Response<Boolean>>
}
