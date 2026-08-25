package com.example.holoverse.search.data.repository

import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.course.domain.Courses
import com.example.holoverse.search.data.local.dao.RecentSearchDao
import com.example.holoverse.search.data.local.entities.RecentSearchEntity
import com.example.holoverse.search.domain.model.CourseFilters
import com.example.holoverse.search.domain.model.MentorFilters
import com.example.holoverse.search.domain.repository.SearchRepository
import com.example.holoverse.search.presentation.SearchType
import com.example.holoverse.core.utils.NetworkConstant.COLLECTION_NAME_USERS
import com.example.holoverse.core.utils.Response
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val recentSearchDao: RecentSearchDao
) : SearchRepository {

    override fun searchCourses(filters: CourseFilters): Flow<Response<List<Courses>>> =
        callbackFlow {
            trySend(Response.Loading)

            var query: Query = firestore.collection("courses")

            if (!filters.query.isNullOrBlank()) {
                query = query.whereGreaterThanOrEqualTo("name", filters.query)
                    .whereLessThanOrEqualTo("name", filters.query + "\uf8ff")
            }

            filters.category?.let {
                query = query.whereEqualTo("category", it.name)
            }
            filters.level?.let {
                query = query.whereEqualTo("level", it)
            }
            filters.minRating?.let {
                query = query.whereGreaterThanOrEqualTo("rating", it)
            }

            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Response.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }

                val courses = snapshot?.toObjects(Courses::class.java) ?: emptyList()

                // In-memory filtering for remaining complex filters
                var filteredCourses = courses

                filters.minPrice?.let { min ->
                    filteredCourses = filteredCourses.filter { it.price >= min }
                }

                filters.maxPrice?.let { max ->
                    filteredCourses = filteredCourses.filter { it.price <= max }
                }

                trySend(Response.Success(filteredCourses))
            }

            awaitClose { listener.remove() }
        }

    override fun searchMentors(filters: MentorFilters): Flow<Response<List<User.Mentor>>> =
        callbackFlow {
            trySend(Response.Loading)

            var query: Query = firestore.collection(COLLECTION_NAME_USERS)
                .whereEqualTo("accountType", "Mentor")

            if (!filters.query.isNullOrBlank()) {
                query = query.whereGreaterThanOrEqualTo("fullName", filters.query)
                    .whereLessThanOrEqualTo("fullName", filters.query + "\uf8ff")
            }

            filters.specialization?.let {
                query = query.whereEqualTo("specialization", it.name)
            }
            filters.minRating?.let {
                query = query.whereGreaterThanOrEqualTo("averageRating", it)
            }

            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Response.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }

                val mentors = snapshot?.toObjects(User.Mentor::class.java) ?: emptyList()

                // In-memory filtering for subjects and hourly rate
                var filteredMentors = mentors

                if (!filters.subjects.isNullOrEmpty()) {
                    filteredMentors = filteredMentors.filter { mentor ->
                        filters.subjects.any { subject -> mentor.subjects?.contains(subject) == true }
                    }
                }

                filters.minHourlyRate?.let { min ->
                    filteredMentors = filteredMentors.filter { (it.hourlyRate ?: 0.0) >= min }
                }

                filters.maxHourlyRate?.let { max ->
                    filteredMentors = filteredMentors.filter { (it.hourlyRate ?: 0.0) <= max }
                }

                trySend(Response.Success(filteredMentors))
            }

            awaitClose { listener.remove() }
        }

    override fun getRecentSearches(userId: String, type: SearchType): Flow<List<String>> {
        return recentSearchDao.getRecentSearches(userId, type.name)
            .map { entities -> entities.map { it.query } }
    }

    override suspend fun saveRecentSearch(user: User, query: String, type: SearchType) {
        val userId = user.userId ?: return
        if (query.isBlank()) return

        // 1. Save to Room
        recentSearchDao.delete(userId, query, type.name) // Remove if exists to update timestamp
        recentSearchDao.insert(
            RecentSearchEntity(
                userId = userId,
                query = query,
                searchType = type.name
            )
        )

        // 2. Save to Firebase
        try {
            val userRef = firestore.collection(COLLECTION_NAME_USERS).document(userId)
            val searchData = mapOf(
                "query" to query,
                "type" to type.name,
                "timestamp" to FieldValue.serverTimestamp()
            )
            
            userRef.collection("recent_searches").add(searchData).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun removeRecentSearch(user: User, query: String, type: SearchType) {
        val userId = user.userId ?: return

        // 1. Remove from Room
        recentSearchDao.delete(userId, query, type.name)

        // 2. Remove from Firebase
        try {
            val userRef = firestore.collection(COLLECTION_NAME_USERS).document(userId)
            val querySnapshot = userRef.collection("recent_searches")
                .whereEqualTo("query", query)
                .whereEqualTo("type", type.name)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                document.reference.delete().await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


