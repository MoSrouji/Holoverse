package com.example.holoverse.courses.data

import android.util.Log
import com.example.holoverse.courses.domain.Courses
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class CourseRepoImpl(private val firestore: FirebaseFirestore) : CourseRepo {

    override suspend fun addCourse(course: Courses) {
        try {
            firestore.collection("courses").add(course).await()
        } catch (e: Exception) {
            Log.e("CourseRepoImpl", "Error adding course", e)
            throw e
        }
    }

    override suspend fun deleteCourse(course: Courses) {
        try {
            firestore.collection("courses").document(course.id).delete().await()
        } catch (e: Exception) {
            Log.e("CourseRepoImpl", "Error deleting course", e)
            throw e
        }
    }

    override suspend fun updateCourse(course: Courses) {
        try {
            firestore.collection("courses").document(course.id).set(course).await()
        } catch (e: Exception) {
            Log.e("CourseRepoImpl", "Error updating course", e)
            throw e
        }
    }

    override suspend fun getCourseById(courseId: String): Courses? {
        return try {
            firestore.collection("courses").document(courseId).get().await()
                .toObject(Courses::class.java)
        } catch (e: Exception) {
            Log.e("CourseRepoImpl", "Error getting course by id", e)
            throw e
        }
    }
}
