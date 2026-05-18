package com.example.holoverse.courses.data

import android.util.Log
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.utils.Response
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class CourseRepoImpl(private val firestore: FirebaseFirestore) : CourseRepo {

    override suspend fun addCourse(course: Courses): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            firestore.collection("courses")
                .document(course.id.ifEmpty { firestore.collection("courses").document().id })
                .set(course)
                .await()
            emit(Response.Success(true))
        } catch (e: Exception) {
            Log.e("CourseRepoImpl", "Error adding course: ${e.message}", e)
            emit(Response.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override suspend fun deleteCourse(course: Courses): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            firestore.collection("courses").document(course.id).delete().await()
            emit(Response.Success(true))
        } catch (e: Exception) {
            Log.e("CourseRepoImpl", "Error deleting course", e)
            emit(Response.Error(e.message ?: "Error deleting course"))
        }
    }

    override suspend fun updateCourse(course: Courses): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            firestore.collection("courses").document(course.id).set(course).await()
            emit(Response.Success(true))
        } catch (e: Exception) {
            Log.e("CourseRepoImpl", "Error updating course", e)
            emit(Response.Error(e.message ?: "Error updating course"))
        }
    }

    override suspend fun getCourseById(courseId: String): Flow<Response<Courses?>> = flow {
        emit(Response.Loading)
        try {
            val snapshot = firestore.collection("courses").document(courseId).get().await()
            val course = snapshot.toObject(Courses::class.java)
            emit(Response.Success(course))
        } catch (e: Exception) {
            Log.e("CourseRepoImpl", "Error getting course by id", e)
            emit(Response.Error(e.message ?: "Error fetching course"))
        }
    }

    override suspend fun getCoursesByCategory(category: String): Flow<Response<List<Courses>>> = flow {
        emit(Response.Loading)
        try {
            val snapshot = firestore.collection("courses")
                .whereEqualTo("category", category)
                .get()
                .await()
            val courses = snapshot.toObjects(Courses::class.java)
            emit(Response.Success(courses))
        } catch (e: Exception) {
            Log.e("CourseRepoImpl", "Error getting courses by category", e)
            emit(Response.Error(e.message ?: "Error fetching courses"))
        }
    }

    override suspend fun getCoursesByInstructorId(instructorId: String): Flow<Response<List<Courses>>> = flow {
        emit(Response.Loading)
        try {
            val snapshot = firestore.collection("courses")
                .whereEqualTo("instructorId", instructorId)
                .get()
                .await()
            val courses = snapshot.toObjects(Courses::class.java)
            emit(Response.Success(courses))
        } catch (e: Exception) {
            Log.e("CourseRepoImpl", "Error getting courses by instructor id", e)
            emit(Response.Error(e.message ?: "Error fetching courses"))
        }
    }
}
