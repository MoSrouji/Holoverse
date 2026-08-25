package com.example.holoverse.fetch.data

import android.content.ContentValues.TAG
import android.util.Log
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.course.domain.BoostedCourse
import com.example.holoverse.course.domain.Courses
import com.example.holoverse.fetch.domain.FetchDataRepository
import com.example.holoverse.core.utils.NetworkConstant
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FetchDataRepositoryImpl(
    private val firestore: FirebaseFirestore
) : FetchDataRepository {

    private var cachedCourses: List<Courses>? = null
    private var cachedMentors: List<User.Mentor>? = null
    private var lastCourseDocCache: DocumentSnapshot? = null
    private var lastMentorDocCache: DocumentSnapshot? = null

    override suspend fun fetchCourses(
        forceRefresh: Boolean,
        limit: Long,
        lastVisible: DocumentSnapshot?
    ): Pair<List<Courses>, DocumentSnapshot?> {
        if (!forceRefresh && cachedCourses != null && lastVisible == null) {
            return Pair(cachedCourses!!, lastCourseDocCache)
        }

        return try {
            var query = firestore.collection("courses")
                .orderBy("numEnrolled", Query.Direction.DESCENDING)
                .limit(limit)

            if (lastVisible != null) {
                query = query.startAfter(lastVisible)
            }

            val snapshot = query.get().await()

            val courses = snapshot.documents.mapNotNull { doc ->
                try {
                    val course = doc.toObject(Courses::class.java) ?: return@mapNotNull null
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
            }

            val newLastVisible = snapshot.documents.lastOrNull()
            
            if (lastVisible == null) {
                cachedCourses = courses
                lastCourseDocCache = newLastVisible
            }
            
            Pair(courses, newLastVisible)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching courses", e)
            Pair(cachedCourses ?: emptyList(), lastCourseDocCache)
        }
    }

    override suspend fun fetchMentors(
        forceRefresh: Boolean,
        limit: Long,
        lastVisible: DocumentSnapshot?
    ): Pair<List<User.Mentor>, DocumentSnapshot?> {
        if (!forceRefresh && cachedMentors != null && lastVisible == null) {
            return Pair(cachedMentors!!, lastMentorDocCache)
        }

        return try {
            var query = firestore.collection(NetworkConstant.COLLECTION_NAME_USERS)
                .whereEqualTo("accountType", "Mentor")
                .limit(limit)

            if (lastVisible != null) {
                query = query.startAfter(lastVisible)
            }

            val snapshot = query.get().await()

            val mentors = snapshot.documents.mapNotNull { doc ->
                try {
                    val mentor = doc.toObject(User.Mentor::class.java) ?: return@mapNotNull null
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
            }

            val newLastVisible = snapshot.documents.lastOrNull()

            if (lastVisible == null) {
                cachedMentors = mentors
                lastMentorDocCache = newLastVisible
            }
            
            Pair(mentors, newLastVisible)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching mentors", e)
            Pair(cachedMentors ?: emptyList(), lastMentorDocCache)
        }
    }

    override suspend fun fetchRecommendedCourses(
        categories: List<String>,
        limit: Int
    ): List<Courses> {
        if (categories.isEmpty()) return emptyList()
        return try {
            Log.d("FetchData", "Fetching recommended courses for: $categories")
            // Firestore whereIn limit is 30, which is enough for categories
            val snapshot = firestore.collection("courses")
                .whereIn("category", categories.take(30))
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val course = doc.toObject(Courses::class.java) ?: return@mapNotNull null
                    val categoryString = doc.getString("category")
                    val courseWithId = course.copy(id = doc.id)
                    if (categoryString != null) {
                        courseWithId.copy(category = AppCategory.fromString(categoryString))
                    } else {
                        course
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error mapping recommended course: ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FetchData", "Error fetching recommended courses: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun fetchRecommendedMentors(
        specializations: List<String>,
        limit: Int
    ): List<User.Mentor> {
        if (specializations.isEmpty()) return emptyList()
        return try {
            Log.d("FetchData", "Fetching recommended mentors for: $specializations")
            val snapshot = firestore.collection(NetworkConstant.COLLECTION_NAME_USERS)
                .whereEqualTo("accountType", "Mentor")
                .whereIn("specialization", specializations.take(30))
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val mentor = doc.toObject(User.Mentor::class.java) ?: return@mapNotNull null
                    val specString = doc.getString("specialization")
                    if (specString != null) {
                        mentor.copy(specialization = AppCategory.fromString(specString))
                    } else {
                        mentor
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error mapping recommended mentor: ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FetchData", "Error fetching recommended mentors: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun fetchMentorById(mentorId: String): User.Mentor? {
        return try {
            val doc = firestore.collection(NetworkConstant.COLLECTION_NAME_USERS)
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
            Log.d("FetchData", "Fetching boosted courses...")
            val now = System.currentTimeMillis()
            val snapshot = firestore.collection("courses")
                .whereEqualTo("isBoosted", true)
                .get()
                .await()
            
            Log.d("FetchData", "Found ${snapshot.size()} documents with isBoosted=true")
            
            if (snapshot.isEmpty) {
                Log.w("FetchData", "NO BOOSTED COURSES FOUND IN FIRESTORE! Field name searched: 'isBoosted'")
            }

            snapshot.documents.mapNotNull { doc ->
                val course = doc.toObject(Courses::class.java)
                val expiry = doc.getLong("boostExpiry") ?: 0L
                // Give a long window for dummy data or check if it's explicitly set to 0
                if (course != null && (expiry > now || expiry == 0L)) {
                    BoostedCourse(
                        courseId = doc.id,
                        courseName = course.name,
                        courseImageUrl = course.imageUrl,
                        courseDescription = course.description,
                        instructorName = course.instructorName,
                        adCardStyle = try { com.example.holoverse.course.domain.AdCardStyle.valueOf(doc.getString("adCardStyle") ?: "STYLE_1") } catch(e: Exception) { com.example.holoverse.course.domain.AdCardStyle.STYLE_1 },
                        endTimestamp = expiry
                    )
                } else {
                    if (course == null) Log.e("FetchData", "Course object null for doc: ${doc.id}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FetchData", "Error fetching boosted courses: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun cleanupExpiredBoosts() {
        try {
            val now = System.currentTimeMillis()
            val snapshot = firestore.collection("courses")
                .whereEqualTo("isBoosted", true)
                .whereLessThan("boostExpiry", now)
                .get()
                .await()
            
            for (doc in snapshot.documents) {
                doc.reference.update(mapOf("isBoosted" to false)).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up expired boosts", e)
        }
    }

    override suspend fun fetchStudentsByIds(studentIds: List<String>): List<User.Student> {
        if (studentIds.isEmpty()) return emptyList()
        
        return try {
            val snapshot = firestore.collection(NetworkConstant.COLLECTION_NAME_USERS)
                .whereIn("userId", studentIds) // Or whereIn(FieldPath.documentId(), studentIds) if userId matches docId
                .whereEqualTo("accountType", "Student")
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
        lastCourseDocCache = null
        lastMentorDocCache = null
    }
}


