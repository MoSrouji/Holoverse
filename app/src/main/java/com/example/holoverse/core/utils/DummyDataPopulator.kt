package com.example.holoverse.core.utils

import android.util.Log
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.course.domain.CourseSession
import com.example.holoverse.course.domain.Courses
import com.example.holoverse.core.utils.NetworkConstant.COLLECTION_NAME_USERS
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class DummyDataPopulator(private val firestore: FirebaseFirestore) {

    suspend fun populateData() {
        try {
            Log.d("DummyDataPopulator", "Starting data population...")
            
            // 1. Create a Default Admin if none exists
            val adminId = "admin_default_id"
            val admin = User.Admin(
                userId = adminId,
                fullName = "System Admin",
                email = "admin@holoverse.com",
                accountType = com.example.holoverse.auth.domain.entities.UserType.Admin,
                createdAt = System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_NAME_USERS).document(adminId).set(admin).await()
            Log.d("DummyDataPopulator", "Default admin created.")

            // 2. Create new dummy data
            val mentors = createDummyMentors()
            val courses = createDummyCourses(mentors)

            // 3. Add Mentors
            for (mentor in mentors) {
                mentor.userId?.let { id ->
                    firestore.collection(COLLECTION_NAME_USERS).document(id).set(mentor).await()
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
            "Linda Garcia", "Robert Taylor", "Dr. Kevin Lee", "Maria Hernandez",
            "Chris Evans", "Anna Muller", "Takashi Yamamoto", "Fatima Al-Sayed",
            "Igor Petrov", "Chloe Dubois", "Rajesh Khanna", "Svetlana Ivanova",
            "Dr. Emily White", "Dr. Marcus Brown", "Angela Yu", "Stephen Grider",
            "Brad Traversy", "Maximilian Schwarzmüller", "Kent C. Dodds", "Dan Abramov",
            "Cory House", "Scott Hanselman"
        )
        val categories = AppCategory.entries.filter { it != AppCategory.OTHER }

        return names.mapIndexed { index, name ->
            val specialization = categories[index % categories.size]
            User.Mentor(
                userId = "mentor_dummy_${index + 1}_${UUID.randomUUID().toString().take(6)}",
                fullName = name,
                email = "${name.replace(" ", ".").lowercase()}.${index}@example.com",
                bio = "Expert educator with over ${5 + index} years of experience in ${
                    specialization.name.lowercase().replace("_", " ")
                }. Dedicated to helping students achieve their full potential in ${specialization.name}.",
                specialization = specialization,
                yearsOfExperience = (5 + index).toString(),
                averageRating = 4.0 + (index % 10) * 0.1,
                reviewsCount = 10 * (index + 1),
                profileImageUrl = "https://i.pravatar.cc/150?u=mentor_dummy_${index + 1}",
                phoneNumber = "+123456789${index}",
                address = "${100 + index} Education St, Knowledge City",
                gender = if (index % 2 == 0) "Female" else "Male",
                universityAttended = "Global University of ${
                    specialization.name.lowercase().replaceFirstChar { it.uppercase() }
                }",
                graduationYear = "${2000 + (index % 24)}",
                hourlyRate = 25.0 + (index * 5),
                followersCount = 100 * (index + 1),
                totalStudentsTaught = 50 * (index + 1),
                successRate = 0.85 + (index % 15) * 0.01,
                createdAt = System.currentTimeMillis() - (index * 3600000L),
                accountType = com.example.holoverse.auth.domain.entities.UserType.Mentor
            )
        }
    }

    private fun createDummyCourses(mentors: List<User.Mentor>): List<Courses> {
        val categories = AppCategory.entries.filter { it != AppCategory.OTHER }
        val levels = listOf("Beginner", "Intermediate", "Advanced")
        val languages = listOf("English", "Spanish", "French", "German", "Arabic")
        
        val courseTemplates = listOf(
            "Introduction to %s",
            "Advanced %s Techniques",
            "Mastering %s",
            "%s for Professionals",
            "Practical %s Applications",
            "%s Fundamentals",
            "Deep Dive into %s",
            "The Art of %s",
            "%s Strategies for Success",
            "Essential %s Guide"
        )

        val courses = mutableListOf<Courses>()
        
        // Ensure at least 20 courses per category for high density
        for (category in categories) {
            for (j in 0 until 20) {
                val mentor = mentors.filter { it.specialization == category }.randomOrNull() ?: mentors.random()
                val level = levels.random()
                val language = languages.random()
                val template = courseTemplates.random()
                
                val catName = category.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }
                val name = template.format(catName)
                
                // We will mark exactly 10 courses as boosted later
                val isBoosted = false 

                courses.add(
                    Courses(
                        id = "course_dummy_${category.name}_${j + 1}_${UUID.randomUUID().toString().take(8)}",
                        name = name,
                        category = category,
                        specialization = category.name.lowercase().replaceFirstChar { it.uppercase() },
                        price = 14.99 + (j * 2.5) % 150,
                        duration = "${4 + (j % 8)} weeks",
                        level = level,
                        rating = 4.0 + (j % 10) * 0.1,
                        numReviews = 10 + j * 2,
                        numEnrolled = 50 + (j * 15),
                        instructorId = mentor.userId ?: "",
                        instructorName = mentor.fullName ?: "",
                        instructor = mentor.fullName ?: "",
                        description = "This course covers comprehensive aspects of $catName, designed for $level students. You will learn key principles and practical skills that are highly relevant in today's market.",
                        language = language,
                        imageUrl = "https://picsum.photos/seed/course_${category.name}_${j}/600/400",
                        completionRate = 0.5 + (j % 5) * 0.1,
                        averageProgress = 0.3 + (j % 7) * 0.1,
                        isBoosted = isBoosted,
                        boostExpiry = 0L,
                        adCardStyle = "STYLE_1",
                        availableTimeSlots = listOf("Morning", "Afternoon", "Evening").shuffled().take(2),
                        sessions = createDummySessions(name),
                        quizzes = emptyList()
                    )
                )
            }
        }
        
        // Randomly boost exactly 10 courses with 1-year expiry
        courses.shuffled().take(10).forEachIndexed { index, course ->
            val updatedCourse = course.copy(
                isBoosted = true,
                boostExpiry = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000L),
                adCardStyle = "STYLE_${(index % 3) + 1}"
            )
            val courseIdx = courses.indexOf(course)
            courses[courseIdx] = updatedCourse
        }
        
        return courses
    }

    private fun createDummySessions(courseName: String): List<CourseSession> {
        return listOf(
            CourseSession("Welcome to $courseName", "2024-09-01", "09:00 AM", "Getting started with the course."),
            CourseSession("Core Modules", "2024-09-08", "09:00 AM", "Exploring the fundamental building blocks."),
            CourseSession("Interactive Workshop", "2024-09-15", "09:00 AM", "Applying knowledge in real-time."),
            CourseSession("Assessment & Review", "2024-09-22", "09:00 AM", "Reviewing progress and finalizing concepts.")
        )
    }
}


