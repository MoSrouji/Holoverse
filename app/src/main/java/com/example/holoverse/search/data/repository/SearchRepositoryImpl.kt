package com.example.holoverse.search.data.repository

import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.search.domain.model.CourseFilters
import com.example.holoverse.search.domain.model.MentorFilters
import com.example.holoverse.search.domain.repository.SearchRepository
import com.example.holoverse.utils.NetworkConstant.COLLECTION_NAME_MENTORS
import com.example.holoverse.utils.Response
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SearchRepository {

    override fun searchCourses(filters: CourseFilters): Flow<Response<List<Courses>>> = callbackFlow {
        trySend(Response.Loading)

        var query: Query = firestore.collection("courses")

        filters.category?.let {
            query = query.whereEqualTo("category", it)
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
            
            // In-memory filtering for query (prefix search on name) and price range
            var filteredCourses = courses
            
            if (!filters.query.isNullOrBlank()) {
                filteredCourses = filteredCourses.filter { it.name.contains(filters.query, ignoreCase = true) }
            }
            
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

    override fun searchMentors(filters: MentorFilters): Flow<Response<List<User.Mentor>>> = callbackFlow {
        trySend(Response.Loading)

        var query: Query = firestore.collection(COLLECTION_NAME_MENTORS)

        filters.specialization?.let {
            query = query.whereEqualTo("specialization", it)
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

            // In-memory filtering for query, subjects, and hourly rate
            var filteredMentors = mentors
            
            if (!filters.query.isNullOrBlank()) {
                filteredMentors = filteredMentors.filter { 
                    it.fullName?.contains(filters.query, ignoreCase = true) == true 
                }
            }
            
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
}