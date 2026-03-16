package com.example.holoverse.search.data.repository

import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.search.domain.model.CourseFilters
import com.example.holoverse.search.domain.model.MentorFilters
import com.example.holoverse.search.domain.repository.SearchRepository
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
        filters.minPrice?.let {
            query = query.whereGreaterThanOrEqualTo("price", it)
        }
        filters.maxPrice?.let {
            query = query.whereLessThanOrEqualTo("price", it)
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
            
            // In-memory filtering for query (prefix search on name)
            val filteredCourses = if (!filters.query.isNullOrBlank()) {
                courses.filter { it.name.contains(filters.query, ignoreCase = true) }
            } else {
                courses
            }

            trySend(Response.Success(filteredCourses))
        }

        awaitClose { listener.remove() }
    }

    override fun searchMentors(filters: MentorFilters): Flow<Response<List<User.Mentor>>> = callbackFlow {
        trySend(Response.Loading)

        var query: Query = firestore.collection("users")
            .whereEqualTo("accountType", "Mentor")

        filters.specialization?.let {
            query = query.whereEqualTo("specialization", it)
        }
        filters.minHourlyRate?.let {
            query = query.whereGreaterThanOrEqualTo("hourlyRate", it)
        }
        filters.maxHourlyRate?.let {
            query = query.whereLessThanOrEqualTo("hourlyRate", it)
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

            // In-memory filtering for query and subjects
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

            trySend(Response.Success(filteredMentors))
        }

        awaitClose { listener.remove() }
    }
}