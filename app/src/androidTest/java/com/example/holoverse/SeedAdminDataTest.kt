package com.example.holoverse

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.entities.UserType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class SeedAdminDataTest {

    private val firestore = FirebaseFirestore.getInstance()

    @Test
    fun seedNewUsers() = runBlocking {
        val now = System.currentTimeMillis()
        
        // Students
        val students = listOf(
            User.Student(
                userId = "seed_student_1",
                fullName = "John Doe",
                email = "john.doe@example.com",
                createdAt = now - (1000 * 60 * 60 * 5) // 5 hours ago
            ),
            User.Student(
                userId = "seed_student_2",
                fullName = "Jane Smith",
                email = "jane.smith@example.com",
                createdAt = now - (1000 * 60 * 60 * 24 * 2) // 2 days ago
            )
        )

        // Mentors
        val mentors = listOf(
            User.Mentor(
                userId = "seed_mentor_1",
                fullName = "Prof. Alice",
                email = "alice@holoverse.edu",
                createdAt = now - (1000 * 60 * 60 * 2) // 2 hours ago
            ),
            User.Mentor(
                userId = "seed_mentor_2",
                fullName = "Dr. Bob",
                email = "bob@holoverse.edu",
                createdAt = now - (1000 * 60 * 60 * 24 * 5) // 5 days ago
            ),
            User.Mentor(
                userId = "seed_mentor_3",
                fullName = "Sarah Wilson",
                email = "sarah.w@holoverse.edu",
                createdAt = now // Just now
            )
        )

        students.forEach { student ->
            firestore.collection("students").document(student.userId!!).set(student).await()
            println("Seeded student: ${student.fullName}")
        }

        mentors.forEach { mentor ->
            firestore.collection("teachers").document(mentor.userId!!).set(mentor).await()
            println("Seeded mentor: ${mentor.fullName}")
        }
    }
}
