package com.example.holoverse.courses.data

import com.example.holoverse.courses.domain.Courses

interface CourseRepo {
    suspend fun addCourse(course: Courses)
    suspend fun deleteCourse(course: Courses)
    suspend fun updateCourse(course: Courses)
    suspend fun getCourseById(courseId: String): Courses?
}
