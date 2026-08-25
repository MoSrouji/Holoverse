package com.example.holoverse.home.domain.model

import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.core.domain.model.AppCategory
import com.example.holoverse.course.domain.BoostedCourse
import com.example.holoverse.course.domain.Courses
import com.google.firebase.firestore.DocumentSnapshot

data class HomeContent(
    val currentUser: User? = null,
    val popularCourses: List<Courses> = emptyList(),
    val recommendedCourses: List<Courses> = emptyList(),
    val enrolledCourses: List<Courses> = emptyList(),
    val savedCourses: List<Courses> = emptyList(),
    val boostedCourses: List<BoostedCourse> = emptyList(),
    val categories: List<AppCategory> = emptyList(),
    val popularMentors: List<User.Mentor> = emptyList(),
    val recommendedMentors: List<User.Mentor> = emptyList(),
    val lastCourseDocument: DocumentSnapshot? = null,
    val lastMentorDocument: DocumentSnapshot? = null
)
