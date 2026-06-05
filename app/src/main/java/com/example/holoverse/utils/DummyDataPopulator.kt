package com.example.holoverse.utils

import android.util.Log
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.courses.domain.CourseSession
import com.example.holoverse.courses.domain.Courses
import com.example.holoverse.utils.NetworkConstant.COLLECTION_NAME_MENTORS
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class DummyDataPopulator(private val firestore: FirebaseFirestore) {

    suspend fun populateData() {
        try {
            Log.d("DummyDataPopulator", "Starting data population...")
            // 1. Delete existing data (Clear collections)
            deleteCollection("courses")
            deleteCollection(COLLECTION_NAME_MENTORS)
            Log.d("DummyDataPopulator", "Cleared existing courses and mentors.")

            // 2. Create new dummy data
            val mentors = createDummyMentors()
            val courses = createDummyCourses(mentors)

            // 3. Add Mentors
            for (mentor in mentors) {
                mentor.userId?.let { id ->
                    firestore.collection(COLLECTION_NAME_MENTORS).document(id).set(mentor).await()
                }
            }
            Log.d("DummyDataPopulator", "Added ${mentors.size} mentors.")

            // 4. Add Courses
            for (course in courses) {
                firestore.collection("courses").document(course.id).set(course).await()
            }
            Log.d("DummyDataPopulator", "Added ${courses.size} courses.")
            Log.d("DummyDataPopulator", "Data population completed successfully!")
        } catch (e: Exception) {
            Log.e("DummyDataPopulator", "Error populating data: ${e.message}", e)
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
        val categories = AppCategory.entries.filter { it != AppCategory.OTHER }

        return names.mapIndexed { index, name ->
            val specialization = categories[index % categories.size]
            User.Mentor(
                userId = "mentor_${index + 1}",
                fullName = name,
                email = "${name.replace(" ", ".").lowercase()}@example.com",
                bio = "Expert educator with over ${5 + index} years of experience in ${
                    specialization.name.lowercase().replace("_", " ")
                }. Dedicated to helping students achieve their full potential.",
                specialization = specialization,
                yearsOfExperience = (5 + index).toString(),
                averageRating = 4.0 + (index % 10) * 0.1,
                reviewsCount = 10 * (index + 1),
                profileImageUrl = "https://i.pravatar.cc/150?u=mentor_${index + 1}",
                phoneNumber = "+123456789${index}",
                address = "${100 + index} Education St, Knowledge City",
                gender = if (index % 2 == 0) "Female" else "Male",
                universityAttended = "Global University of ${
                    specialization.name.lowercase().replaceFirstChar { it.uppercase() }
                }",
                graduationYear = "${2000 + index}",
                hourlyRate = 25.0 + (index * 5),
                followersCount = 100 * (index + 1),
                totalStudentsTaught = 50 * (index + 1),
                successRate = 0.85 + (index % 15) * 0.01
            )
        }
    }

    private fun createDummyCourses(mentors: List<User.Mentor>): List<Courses> {
        val courseData = listOf(
            Triple(
                "Mastering Calculus",
                AppCategory.MATHEMATICS,
                "Advanced techniques for differentiation and integration."
            ),
            Triple(
                "Kotlin for Professionals",
                AppCategory.COMPUTER_SCIENCE,
                "Deep dive into Kotlin features for Android development."
            ),
            Triple(
                "Art History 101",
                AppCategory.ARTS,
                "A journey through the major art movements from Renaissance to Modernism."
            ),
            Triple(
                "Quantum Physics Intro",
                AppCategory.SCIENCE,
                "Understanding the fundamental principles of quantum mechanics."
            ),
            Triple(
                "Business Strategy 2024",
                AppCategory.BUSINESS,
                "Modern frameworks for competitive advantage in the digital age."
            ),
            Triple(
                "Advanced Yoga Flows",
                AppCategory.HEALTH_FITNESS,
                "Enhance your practice with complex asanas and breathing techniques."
            ),
            Triple(
                "Spanish for Travelers",
                AppCategory.LANGUAGE_LEARNING,
                "Essential vocabulary and phrases for your next trip to Spain or Latin America."
            ),
            Triple(
                "Cybersecurity Basics",
                AppCategory.COMPUTER_SCIENCE,
                "Protect yourself and your data from online threats."
            ),
            Triple(
                "Digital Marketing",
                AppCategory.BUSINESS,
                "Master SEO, SEM, and social media marketing strategies."
            ),
            Triple(
                "Modern Architecture",
                AppCategory.ARTS,
                "Exploring the evolution of architectural design in the 21st century."
            ),
            Triple(
                "Macroeconomics Deep Dive",
                AppCategory.HUMANITIES,
                "Analyzing global economic trends and policy impacts."
            ),
            Triple(
                "Organic Chemistry",
                AppCategory.SCIENCE,
                "Study of the structure, properties, and reactions of organic compounds."
            ),
            Triple(
                "UI/UX Design Principles",
                AppCategory.COMPUTER_SCIENCE,
                "Learn to create user-friendly and aesthetically pleasing interfaces."
            ),
            Triple(
                "Creative Writing Workshop",
                AppCategory.ARTS,
                "Develop your voice and improve your storytelling skills."
            ),
            Triple(
                "Stock Market Investing",
                AppCategory.BUSINESS,
                "Strategies for building a diversified and profitable portfolio."
            ),
            Triple(
                "Psychology of Success",
                AppCategory.HUMANITIES,
                "Understanding the mindset and habits of high achievers."
            ),
            Triple(
                "Machine Learning",
                AppCategory.COMPUTER_SCIENCE,
                "Foundational concepts and algorithms for AI-driven solutions."
            ),
            Triple(
                "World History Overview",
                AppCategory.HUMANITIES,
                "Key events that shaped our modern world from ancient times."
            ),
            Triple(
                "Nutrition Essentials",
                AppCategory.HEALTH_FITNESS,
                "Science-based approach to healthy eating and wellness."
            ),
            Triple(
                "Music Theory",
                AppCategory.ARTS,
                "The building blocks of music: melody, harmony, and rhythm."
            )
        )

        return courseData.mapIndexed { index, (name, category, desc) ->
            val mentor = mentors[index % mentors.size]
            Courses(
                id = UUID.randomUUID().toString(),
                name = name,
                category = category,
                price = 19.99 + (index * 7.5),
                duration = "${6 + (index % 6)} weeks",
                level = when (index % 3) {
                    0 -> "Beginner"; 1 -> "Intermediate"; else -> "Advanced"
                },
                rating = 4.2 + (index % 8) * 0.1,
                numReviews = 15 + index * 3,
                numEnrolled = 120 + (index * 25),
                instructorId = mentor.userId ?: "",
                instructorName = mentor.fullName ?: "",
                instructor = mentor.fullName ?: "",
                description = desc,
                imageUrl = "https://picsum.photos/seed/${index + 500}/600/400",
                completionRate = 0.6 + (index % 4) * 0.1,
                averageProgress = 0.4 + (index % 6) * 0.1,
                sessions = listOf(
                    CourseSession(
                        "Introduction to $name",
                        "2024-06-01",
                        "10:00 AM",
                        "Overview of course objectives and syllabus."
                    ),
                    CourseSession(
                        "Core Concepts Part 1",
                        "2024-06-08",
                        "10:00 AM",
                        "Diving into the fundamental principles."
                    ),
                    CourseSession(
                        "Practical Application",
                        "2024-06-15",
                        "10:00 AM",
                        "Hands-on exercises and real-world examples."
                    ),
                    CourseSession(
                        "Advanced Topics",
                        "2024-06-22",
                        "10:00 AM",
                        "Exploring complex scenarios and edge cases."
                    ),
                    CourseSession(
                        "Final Project Review",
                        "2024-06-29",
                        "10:00 AM",
                        "Discussion and feedback on course projects."
                    )
                )
            )
        }
    }
}
