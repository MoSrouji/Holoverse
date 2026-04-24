package com.example.holoverse.utils

import com.example.holoverse.auth.domain.entities.MentorCategory
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.utils.NetworkConstant.COLLECTION_NAME_MENTORS
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class DummyDataPopulator(private val firestore: FirebaseFirestore) {

    suspend fun populateData() {
        // 1. Delete existing data (Clear collections)
        deleteCollection("courses")
        deleteCollection(COLLECTION_NAME_MENTORS)

        // 2. Create new dummy data
        val mentors = createDummyMentors()
        val courses = createDummyCourses(mentors)

        // 3. Add Mentors
        for (mentor in mentors) {
            mentor.userId?.let { id ->
                firestore.collection(COLLECTION_NAME_MENTORS).document(id).set(mentor).await()
            }
        }

        // 4. Add Courses
        for (course in courses) {
            firestore.collection("courses").document(course.id).set(course).await()
        }
    }

    private suspend fun deleteCollection(collectionPath: String) {
        val collection = firestore.collection(collectionPath)
        val snapshot = collection.get().await()
        for (doc in snapshot.documents) {
            doc.reference.delete().await()
        }
    }

    private fun createDummyMentors(): List<User.Mentor> {
        val names = listOf(
            "Dr. Alice Smith", "Prof. Bob Johnson", "Sarah Williams", "Michael Chen", 
            "Elena Rodriguez", "David Wilson", "Sophie Martin", "James Anderson",
            "Linda Garcia", "Robert Taylor"
        )
        val categories = MentorCategory.entries.toTypedArray()

        return names.mapIndexed { index, name ->
            User.Mentor(
                userId = "mentor_${index + 1}",
                fullName = name,
                email = "${name.replace(" ", ".").lowercase()}@example.com",
                bio = "Expert educator with years of experience in ${categories[index % categories.size].name}.",
                specialization = categories[index % categories.size],
                yearsOfExperience = (5 + index).toString(),
                averageRating = 4.0 + (index % 10) * 0.1,
                reviewsCount = 10 * (index + 1),
                profileImageUrl = "https://i.pravatar.cc/150?u=mentor_${index + 1}"
            )
        }
    }

    private fun createDummyCourses(mentors: List<User.Mentor>): List<Courses> {
        val courseNames = listOf(
            "Mastering Calculus", "Kotlin for Professionals", "Art History 101",
            "Quantum Physics Intro", "Business Strategy 2024", "Advanced Yoga Flows",
            "Cooking Italian Classics", "Cybersecurity Basics", "Modern Architecture",
            "Digital Marketing Masterclass", "Spanish for Travelers", "Data Science with R",
            "Music Theory Essentials", "Macroeconomics Deep Dive", "Organic Chemistry",
            "UI/UX Design Principles", "Creative Writing Workshop", "Stock Market Investing",
            "Psychology of Success", "Machine Learning Foundations"
        )

        val categories = listOf(
            "MATHEMATICS", "COMPUTER_SCIENCE", "ARTS", "SCIENCE", "BUSINESS", 
            "OTHER", "OTHER", "COMPUTER_SCIENCE", "ARTS", "BUSINESS",
            "LANGUAGES", "COMPUTER_SCIENCE", "ARTS", "BUSINESS", "SCIENCE",
            "COMPUTER_SCIENCE", "ARTS", "BUSINESS", "HUMANITIES", "COMPUTER_SCIENCE"
        )

        return courseNames.mapIndexed { index, name ->
            val mentor = mentors[index % mentors.size]
            Courses(
                id = UUID.randomUUID().toString(),
                name = name,
                category = categories[index % categories.size],
                price = 19.99 + (index * 5),
                duration = "${4 + (index % 8)} weeks",
                level = when(index % 3) { 0 -> "Beginner"; 1 -> "Intermediate"; else -> "Advanced" },
                rating = 4.0 + (index % 10) * 0.1,
                numReviews = 10 + index,
                numEnrolled = 50 + (index * 10),
                instructorId = mentor.userId ?: "",
                instructorName = mentor.fullName ?: "",
                description = "This is a comprehensive course on $name, taught by ${mentor.fullName}.",
                imageUrl = "https://picsum.photos/seed/${index + 100}/400/300"
            )
        }
    }
}
