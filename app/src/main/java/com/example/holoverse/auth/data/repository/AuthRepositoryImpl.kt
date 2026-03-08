package com.example.holoverse.auth.data.repository

import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.entities.UserType
import com.example.holoverse.auth.domain.repositiory.AuthRepository
import com.example.holoverse.utils.NetworkConstant.COLLECTION_NAME_STUDENTS
import com.example.holoverse.utils.NetworkConstant.COLLECTION_NAME_MENTORS
import com.example.holoverse.utils.PreferenceManager
import com.example.holoverse.utils.Response
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val preferenceManager: PreferenceManager
) : AuthRepository {
    override suspend fun firebaseSignUp(
        userDto: User,
        password: String
    ): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            val authResult =
                firebaseAuth.createUserWithEmailAndPassword(userDto.email!!, password).await()
            val user = authResult.user ?: throw Exception("User creation failed")

            val userId = user.uid

            val savedUser = when (userDto.accountType) {
                UserType.Student -> {
                    val userDoc = firestore.collection(COLLECTION_NAME_STUDENTS)
                        .document(userId)

                    if (userDoc.get().await().exists()) {
                        // Clean up auth user if document exists
                        user.delete().await()
                        throw Exception("Sign up failed ")
                    }
                    val student = User.Student(
                        fullName = userDto.fullName,
                        email = userDto.email,
                        userId = userId
                    )
                    userDoc.set(student).await()
                    student
                }

                UserType.Mentor -> {
                    val userDoc = firestore.collection(COLLECTION_NAME_MENTORS)
                        .document(userId)
                    if (userDoc.get().await().exists()) {
                        // Clean up auth user if document exists
                        user.delete().await()
                        throw Exception("Sign up failed ")
                    }

                    val mentor = User.Mentor(
                        fullName = userDto.fullName,
                        email = userDto.email,
                        userId = userId
                    )
                    userDoc.set(mentor).await()
                    mentor
                }
            }
            
            preferenceManager.saveUser(savedUser)
            emit(Response.Success(true))

        } catch (e: Exception) {
            emit(Response.Error(e.message ?: "Sign up failed"))
        }
    }

    override suspend fun firebaseSignIn(
        email: String,
        password: String
    ): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)

        try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()

            // Additional checks if needed
            if (authResult.user != null) {
                val user = getCurrentUser()
                user?.let { preferenceManager.saveUser(it) }
                emit(Response.Success(true))
            } else {
                emit(Response.Error("Sign in failed - no user returned"))
            }
        } catch (e: Exception) {
            emit(Response.Error(e.message ?: "Sign in failed"))
        }


    }

    override suspend fun firebaseSignOut(): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            firebaseAuth.signOut()
            preferenceManager.clearData()
            emit(Response.Success(true))
        } catch (e: Exception) {
            emit(Response.Error(e.localizedMessage ?: "Sign out failed"))
        }
    }

    override suspend fun updateMentorProfile(mentor: User.Mentor): Flow<Response<Boolean>> =
        flow {
            emit(Response.Loading)
            try {
                val userId =
                    firebaseAuth.currentUser?.uid ?: throw Exception("User not authenticated")

                val teacherDoc = firestore.collection(COLLECTION_NAME_MENTORS)
                    .document(userId)

                // Verify document exists in single operation
                val snapshot = teacherDoc.get().await()
                if (!snapshot.exists()) {
                    throw Exception("Mentor profile not found")
                }

                // Build update data
                val updateData = buildMap<String, Any> {
                    mentor.fullName?.let { put("fullName", it) }
                    mentor.bio?.let { put("bio", it) }
                    mentor.dateOfBirth?.let { put("dateOfBirth", it) }
                    mentor.phoneNumber?.let { put("phoneNumber", it) }
                    mentor.address?.let { put("address", it) }
                    mentor.gender?.let { put("gender", it) }
                    mentor.yearsOfExperience?.let { put("yearsOfExperience", it) }
                    put("specialization", mentor.specialization.name)
                    mentor.subjects?.let { put("subjects", it) }
                    mentor.certifications?.let { put("certifications", it) }
                    mentor.languagesSpoken?.let { put("languagesSpoken", it) }
                    mentor.hourlyRate?.let { put("hourlyRate", it) }
                    mentor.universityAttended?.let { put("universityAttended", it) }
                    mentor.graduationYear?.let { put("graduationYear", it) }
                    mentor.additionalQualifications?.let { put("additionalQualifications", it) }
                }

                teacherDoc.update(updateData).await()
                
                // Refresh local cache
                val updatedUser = getCurrentUser()
                updatedUser?.let { preferenceManager.saveUser(it) }
                
                emit(Response.Success(true))

            } catch (e: Exception) {
                emit(Response.Error(e.message ?: "Profile update failed"))
            }
        }

    override suspend fun updateStudentProfile(student: User.Student): Flow<Response<Boolean>> =
        flow {
            emit(Response.Loading)
            try {
                val userId =
                    firebaseAuth.currentUser?.uid ?: throw Exception("User not authenticated")

                val studentDoc = firestore.collection(COLLECTION_NAME_STUDENTS)
                    .document(userId)

                val snapshot = studentDoc.get().await()
                if (!snapshot.exists()) {
                    throw Exception("Student profile not found")
                }

                val updateData = buildMap<String, Any> {
                    student.fullName?.let { put("fullName", it) }
                    student.phoneNumber?.let { put("phoneNumber", it) }
                    // Add other fields as needed
                }

                studentDoc.update(updateData).await()
                
                // Refresh local cache
                val updatedUser = getCurrentUser()
                updatedUser?.let { preferenceManager.saveUser(it) }

                emit(Response.Success(true))

            } catch (e: Exception) {
                emit(Response.Error(e.message ?: "Profile update failed"))
            }

        }

    override suspend fun getCurrentUser(): User? {
        val currentUser = firebaseAuth.currentUser ?: return null
        val uid = currentUser.uid

        return try {
            // Check students collection first
            val studentDoc = firestore.collection(COLLECTION_NAME_STUDENTS).document(uid).get().await()
            if (studentDoc.exists()) {
                return studentDoc.toObject(User.Student::class.java)?.copy(userId = uid)
            }

            // If not found, check teachers collection
            val teacherDoc = firestore.collection(COLLECTION_NAME_MENTORS).document(uid).get().await()
            if (teacherDoc.exists()) {
                return teacherDoc.toObject(User.Mentor::class.java)?.copy(userId = uid)
            }

            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    override suspend fun updateUser(user: User?, newEmail: String?): Boolean {
        // Implementation for general update logic if needed
        return true
    }
}
