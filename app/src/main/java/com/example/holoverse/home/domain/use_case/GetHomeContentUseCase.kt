package com.example.holoverse.home.domain.use_case

import android.app.Application
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.course.domain.Courses
import com.example.holoverse.fetch.domain.FetchDataRepository
import com.example.holoverse.home.domain.model.HomeContent
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetHomeContentUseCase @Inject constructor(
    private val fetchDataRepository: FetchDataRepository,
    private val authRepository: AuthRepository,
    private val application: Application
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): HomeContent = coroutineScope {
        fetchDataRepository.cleanupExpiredBoosts()

        val user = authRepository.getCachedUser() ?: authRepository.getCurrentUser()
        
        val userInterests = when (user) {
            is User.Student -> (user.favouriteSubjects ?: emptyList()) + (user.academicInterests ?: emptyList())
            is User.Mentor -> user.subjects ?: emptyList()
            else -> emptyList()
        }.distinct().map { it.trim().replace(" ", "_").uppercase() }

        // 1. Fetch a large pool of courses for diversity (Popular + All)
        // Increased to 60 to ensure we cross category boundaries in case of ties
        val coursesDeferred = async { fetchDataRepository.fetchCourses(forceRefresh, limit = 60) }
        
        // 2. Fetch mentors (already fixed to not use failing orderBy in repository)
        val mentorsDeferred = async { fetchDataRepository.fetchMentors(forceRefresh, limit = 30) }
        
        // 3. Fetch recommendations specifically from Firestore
        val recommendedCoursesDeferred = async { 
            if (userInterests.isEmpty()) {
                emptyList<Courses>()
            } else {
                fetchDataRepository.fetchRecommendedCourses(userInterests, limit = 20)
            }
        }
        val recommendedMentorsDeferred = async { 
            if (userInterests.isEmpty()) {
                emptyList<User.Mentor>()
            } else {
                fetchDataRepository.fetchRecommendedMentors(userInterests, limit = 20)
            }
        }

        val (rawCourses, lastCourseDoc) = coursesDeferred.await()
        val (rawMentors, lastMentorDoc) = mentorsDeferred.await()
        val firestoreRecommendedCourses = recommendedCoursesDeferred.await()
        val firestoreRecommendedMentors = recommendedMentorsDeferred.await()

        // 4. Local Matching Fallback (Crucial for diversity and reliability)
        // Even if Firestore recommended query returns nothing, we try to match against the 60 fetched courses
        val localMatchedCourses = matchCoursesLocally(rawCourses, userInterests)
        val finalRecommendedCourses = if (userInterests.isEmpty()) {
            rawCourses.take(15) // Fallback for guest
        } else {
            (firestoreRecommendedCourses + localMatchedCourses).distinctBy { it.id }
        }

        val finalRecommendedMentors = if (userInterests.isEmpty()) {
            rawMentors.take(10)
        } else {
            (firestoreRecommendedMentors + matchMentorsLocally(rawMentors, userInterests)).distinctBy { it.userId }
        }

        // 5. Derive Categories
        val categories = (listOf(AppCategory.OTHER) + (rawCourses + firestoreRecommendedCourses)
            .map { it.category }
            .distinct()
            .filter { it != AppCategory.OTHER }
            .sortedBy { it.name })

        // 6. Section Specific filtering
        val enrolledCoursesIds = when (user) {
            is User.Student -> user.enrolledCourses ?: emptyList()
            is User.Mentor -> user.enrolledCourses ?: emptyList()
            else -> emptyList()
        }
        val enrolledCourses = (rawCourses + firestoreRecommendedCourses).filter { it.id in enrolledCoursesIds }

        val savedCoursesIds = when (user) {
            is User.Student -> user.savedCourses ?: emptyList()
            is User.Mentor -> user.savedCourses ?: emptyList()
            else -> emptyList()
        }
        val savedCourses = (rawCourses + firestoreRecommendedCourses).filter { it.id in savedCoursesIds }

        HomeContent(
            currentUser = user,
            popularCourses = rawCourses,
            recommendedCourses = finalRecommendedCourses,
            enrolledCourses = enrolledCourses,
            savedCourses = savedCourses,
            categories = categories,
            popularMentors = rawMentors,
            recommendedMentors = finalRecommendedMentors,
            lastCourseDocument = lastCourseDoc,
            lastMentorDocument = lastMentorDoc
        )
    }

    private fun matchCoursesLocally(courses: List<Courses>, interests: List<String>): List<Courses> {
        if (interests.isEmpty()) return emptyList()
        return courses.filter { course ->
            interests.any { interest ->
                val catName = course.category.name.uppercase()
                if (catName == interest) return@any true
                
                course.category.specializations.any { specRes ->
                    val spec = try { application.getString(specRes) } catch (e: Exception) { "" }
                    spec.trim().replace(" ", "_").uppercase() == interest
                }
            }
        }
    }

    private fun matchMentorsLocally(mentors: List<User.Mentor>, interests: List<String>): List<User.Mentor> {
        if (interests.isEmpty()) return emptyList()
        return mentors.filter { mentor ->
            interests.any { interest ->
                val specName = mentor.specialization?.name?.uppercase()
                if (specName == interest) return@any true
                
                mentor.specialization?.specializations?.any { specRes ->
                    val spec = try { application.getString(specRes) } catch (e: Exception) { "" }
                    spec.trim().replace(" ", "_").uppercase() == interest
                } ?: false
            }
        }
    }
}
