package com.example.holoverse.course.data

import android.util.Log
import com.example.holoverse.course.domain.BoostedCourse
import com.example.holoverse.course.domain.Courses
import com.example.holoverse.course.domain.QuizResult
import com.example.holoverse.core.utils.Response
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
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
            if (e is CancellationException) throw e
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
            if (e is CancellationException) throw e
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
            if (e is CancellationException) throw e
            Log.e("CourseRepoImpl", "Error updating course", e)
            emit(Response.Error(e.message ?: "Error updating course"))
        }
    }

    override suspend fun getCourseById(courseId: String): Flow<Response<Courses?>> = flow {
        emit(Response.Loading)
        try {
            val snapshot = firestore.collection("courses").document(courseId).get().await()
            val course = snapshot.toObject(Courses::class.java)
            if (course != null) {
                // Ensure ID is set and category is correctly mapped
                val categoryString = snapshot.getString("category")
                val courseWithId = course.copy(
                    id = snapshot.id,
                    category = if (categoryString != null) com.example.holoverse.core.domain.model.AppCategory.fromString(
                        categoryString
                    ) else course.category
                )
                emit(Response.Success(courseWithId))
            } else {
                emit(Response.Success(null))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("CourseRepoImpl", "Error getting course by id", e)
            emit(Response.Error(e.message ?: "Error fetching course"))
        }
    }

    override suspend fun getCoursesByCategory(category: com.example.holoverse.core.domain.model.AppCategory): Flow<Response<List<Courses>>> =
        flow {
            emit(Response.Loading)
            try {
                val snapshot = firestore.collection("courses")
                    .whereEqualTo("category", category.name) // Use .name to match Firestore storage
                    .get()
                    .await()
                val courses = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Courses::class.java)?.let { course ->
                        val categoryString = doc.getString("category")
                        course.copy(
                            id = doc.id,
                            category = if (categoryString != null) com.example.holoverse.core.domain.model.AppCategory.fromString(
                                categoryString
                            ) else course.category
                        )
                    }
                }
                emit(Response.Success(courses))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("CourseRepoImpl", "Error getting courses by category", e)
                emit(Response.Error(e.message ?: "Error fetching courses"))
            }
        }

    override suspend fun getCoursesByInstructorId(instructorId: String): Flow<Response<List<Courses>>> =
        flow {
            emit(Response.Loading)
            try {
                val snapshot = firestore.collection("courses")
                    .whereEqualTo("instructorId", instructorId)
                    .get()
                    .await()
                val courses = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Courses::class.java)?.let { course ->
                        val categoryString = doc.getString("category")
                        course.copy(
                            id = doc.id,
                            category = if (categoryString != null) com.example.holoverse.core.domain.model.AppCategory.fromString(
                                categoryString
                            ) else course.category
                        )
                    }
                }
                emit(Response.Success(courses))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("CourseRepoImpl", "Error getting courses by instructor id", e)
                emit(Response.Error(e.message ?: "Error fetching courses"))
            }
        }

    override suspend fun boostCourse(boostedCourse: BoostedCourse): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            firestore.collection("boostedCourses")
                .document(boostedCourse.courseId)
                .set(boostedCourse)
                .await()
            emit(Response.Success(true))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("CourseRepoImpl", "Error boosting course", e)
            emit(Response.Error(e.message ?: "Error boosting course"))
        }
    }

    override suspend fun getBoostedCourses(): Flow<Response<List<BoostedCourse>>> = flow {
        emit(Response.Loading)
        try {
            val snapshot = firestore.collection("boostedCourses").get().await()
            val boostedCourses = snapshot.toObjects(BoostedCourse::class.java)
            emit(Response.Success(boostedCourses))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("CourseRepoImpl", "Error getting boosted courses", e)
            emit(Response.Error(e.message ?: "Error fetching boosted courses"))
        }
    }

    override suspend fun deleteBoostedCourse(courseId: String): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            firestore.collection("boostedCourses").document(courseId).delete().await()
            emit(Response.Success(true))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("CourseRepoImpl", "Error deleting boosted course", e)
            emit(Response.Error(e.message ?: "Error deleting boosted course"))
        }
    }

    override suspend fun saveQuizResult(result: QuizResult): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            firestore.collection("quizResults")
                .document(result.id.ifEmpty { firestore.collection("quizResults").document().id })
                .set(result)
                .await()
            emit(Response.Success(true))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("CourseRepoImpl", "Error saving quiz result: ${e.message}", e)
            emit(Response.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override suspend fun getQuizResults(userId: String, courseId: String): Flow<Response<List<QuizResult>>> = flow {
        emit(Response.Loading)
        try {
            val snapshot = firestore.collection("quizResults")
                .whereEqualTo("userId", userId)
                .whereEqualTo("courseId", courseId)
                .get()
                .await()
            val results = snapshot.toObjects(QuizResult::class.java)
            emit(Response.Success(results))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("CourseRepoImpl", "Error getting quiz results: ${e.message}", e)
            emit(Response.Error(e.message ?: "Error fetching quiz results"))
        }
    }
}


