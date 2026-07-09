package com.example.holoverse.fetch.data

import android.content.ContentValues.TAG
import android.util.Log
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.courses.domain.BoostedCourse
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.fetch.domain.FetchDataRepository
import com.example.holoverse.utils.NetworkConstant
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FetchDataRepositoryImpl(
    private val firestore: FirebaseFirestore
) : FetchDataRepository {

    private var cachedCourses: List<Courses>? = null
    private var cachedMentors: List<User.Mentor>? = null

    override suspend fun fetchCourses(forceRefresh: Boolean): List<Courses> {
        if (!forceRefresh && cachedCourses != null) {
            return cachedCourses!!
        }

        return try {
            val snapshot = firestore.collection("courses")
                .get()
                .await()

            val courses = snapshot.documents.mapNotNull { doc ->
                try {
                    val course = doc.toObject(Courses::class.java) ?: return@mapNotNull null
                    // Fix for AppCategory deserialization and missing ID
                    val categoryString = doc.getString("category")
                    val courseWithId = course.copy(id = doc.id)
                    if (categoryString != null) {
                        courseWithId.copy(category = AppCategory.fromString(categoryString))
                    } else {
                        course
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error mapping course: ${doc.id}", e)
                    null
                }
            }.shuffled()

            cachedCourses = courses
            courses
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching courses", e)
            cachedCourses ?: emptyList()
        }
    }

    override suspend fun fetchMentors(forceRefresh: Boolean): List<User.Mentor> {
        if (!forceRefresh && cachedMentors != null) {
            return cachedMentors!!
        }

        return try {
            val snapshot = firestore.collection(NetworkConstant.COLLECTION_NAME_MENTORS)
                .get()
                .await()

            val mentors = snapshot.documents.mapNotNull { doc ->
                try {
                    val mentor = doc.toObject(User.Mentor::class.java) ?: return@mapNotNull null
                    // Fix for AppCategory deserialization
                    val specString = doc.getString("specialization")
                    if (specString != null) {
                        mentor.copy(specialization = AppCategory.fromString(specString))
                    } else {
                        mentor
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error mapping mentor: ${doc.id}", e)
                    null
                }
            }.shuffled()

            cachedMentors = mentors
            mentors
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching mentors", e)
            cachedMentors ?: emptyList()
        }
    }

    override suspend fun fetchMentorById(mentorId: String): User.Mentor? {
        return try {
            val doc = firestore.collection(NetworkConstant.COLLECTION_NAME_MENTORS)
                .document(mentorId)
                .get()
                .await()

            val mentor = doc.toObject(User.Mentor::class.java) ?: return null
            // Fix for AppCategory deserialization
            val specString = doc.getString("specialization")
            if (specString != null) {
                mentor.copy(specialization = AppCategory.fromString(specString))
            } else {
                mentor
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching mentor by id: $mentorId", e)
            null
        }
    }

    override suspend fun fetchAds() {
        // Implementation for fetchAds depends on what the return type and data model should be.
        Log.d(TAG, "fetchAds: Not yet implemented")
    }

    override suspend fun fetchBoostedCourses(): List<BoostedCourse> {
        return try {
            val snapshot = firestore.collection("boostedCourses")
                .get()
                .await()
            snapshot.toObjects(BoostedCourse::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching boosted courses", e)
            emptyList()
        }
    }

    override suspend fun cleanupExpiredBoosts() {
        try {
            val now = System.currentTimeMillis()
            val snapshot = firestore.collection("boostedCourses")
                .whereLessThan("endTimestamp", now)
                .get()
                .await()
            
            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up expired boosts", e)
        }
    }

    override suspend fun fetchStudentsByIds(studentIds: List<String>): List<User.Student> {
        if (studentIds.isEmpty()) return emptyList()
        
        return try {
            val snapshot = firestore.collection(NetworkConstant.COLLECTION_NAME_STUDENTS)
                .whereIn("userId", studentIds)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(User.Student::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Error mapping student: ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching students by ids", e)
            emptyList()
        }
    }

    override fun clearCache() {
        cachedCourses = null
        cachedMentors = null
    }
}
