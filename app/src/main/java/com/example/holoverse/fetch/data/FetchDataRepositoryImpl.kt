package com.example.holoverse.fetch.data

import android.content.ContentValues.TAG
import android.util.Log
import com.example.holoverse.auth.domain.entities.User
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
            val courses = firestore.collection("courses")
                .get()
                .await()
                .toObjects(Courses::class.java)
                .shuffled()
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
            val mentors = firestore.collection(NetworkConstant.COLLECTION_NAME_MENTORS)
                .get()
                .await()
                .toObjects(User.Mentor::class.java)
                .shuffled()
            cachedMentors = mentors
            mentors
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching mentors", e)
            cachedMentors ?: emptyList()
        }
    }

    override suspend fun fetchMentorById(mentorId: String): User.Mentor? {
        return try {
            firestore.collection(NetworkConstant.COLLECTION_NAME_MENTORS)
                .document(mentorId)
                .get()
                .await()
                .toObject(User.Mentor::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching mentor by id: $mentorId", e)
            null
        }
    }

    override suspend fun fetchAds() {
        // Implementation for fetchAds depends on what the return type and data model should be.
        Log.d(TAG, "fetchAds: Not yet implemented")
    }
}
