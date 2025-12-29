package com.example.holoverse.auth.data.repository

import com.example.holoverse.auth.domain.entities.Student
import com.example.holoverse.auth.domain.entities.Teacher
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.entities.UserType
import com.example.holoverse.auth.domain.repositiory.AuthRepository
import com.example.holoverse.auth.network.NetworkConstant
import com.example.holoverse.utils.Response
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore

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

            when (userDto.accountType) {
                UserType.Student -> {
                    val userDoc = firestore.collection(NetworkConstant.COLLECTION_NAME_STUDENTS)
                        .document(userId)

                    if (userDoc.get().await().exists()) {
                        // Clean up auth user if document exists
                        user.delete().await()
                        throw Exception("Sign up failed ")
                    }
                    userDoc.set(
                        Student(
                            fullName = userDto.fullName,
                            email = userDto.email,
                            userId = userId
                        )
                    ).await()


                }

                UserType.Teacher -> {
                    val userDoc = firestore.collection(NetworkConstant.COLLECTION_NAME_TEACHERS)
                        .document(userId)
                    if (userDoc.get().await().exists()) {
                        // Clean up auth user if document exists
                        user.delete().await()
                        throw Exception("Sign up failed ")
                    }

                    userDoc.set(
                        Teacher(
                            fullName = userDto.fullName,
                            email = userDto.email,
                            userId = userId
                        )

                    ).await()


                }

                else -> {}
            }



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
            emit(Response.Success(true))
        } catch (e: Exception) {
            emit(Response.Error(e.localizedMessage ?: "Sign out failed"))
        }
    }

    override suspend fun updateTeacherProfile(teacher: Teacher): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            val userId = firebaseAuth.currentUser?.uid ?: throw Exception("User not authenticated")

            val teacherDoc = firestore.collection(NetworkConstant.COLLECTION_NAME_TEACHERS)
                .document(userId)

            // Verify document exists in single operation
            val snapshot = teacherDoc.get().await()
            if (!snapshot.exists()) {
                throw Exception("Teacher profile not found")
            }

            // Build update data
            val updateData = buildMap<String, Any> {
                //put("fullName", teacher.fullName ?: throw Exception("Full name is required"))
                teacher.bio?.let { put("bio", it) }
                // Personal Information
                teacher.dateOfBirth?.let { put("dateOfBirth", it) }
                teacher.phoneNumber?.let { put("phoneNumber", it) }
                teacher.address?.let { put(" address", it) }
                teacher.gender?.let { put(" gender", it) }
                // Professional Information
                teacher.yearsOfExperience?.let { put(" yearsOfExperience", it) }
                teacher.specialization?.let { put("specialization", it) }
                teacher.subjects?.let { put("subjects", it) }
                teacher.certifications?.let { put("certifications", it) }
                teacher.languagesSpoken?.let { put("languagesSpoken", it) }
                teacher.hourlyRate?.let { put("hourlyRate", it) }
                // Educational Background
                teacher.universityAttended?.let { put("universityAttended", it) }
                teacher.graduationYear?.let { put("graduationYear", it) }
                teacher.additionalQualifications?.let { put("additionalQualifications", it) }
                // Educational Background
                teacher.universityAttended?.let { put("universityAttended", it) }
                teacher.graduationYear?.let { put("graduationYear", it) }
                teacher.additionalQualifications?.let { put("additionalQualifications", it) }


            }

            teacherDoc.update(updateData).await()
            emit(Response.Success(true))

        } catch (e: Exception) {
            emit(Response.Error(e.message ?: "Profile update failed"))
        }
    }

    override suspend fun updateStudentProfile(student: Student): Flow<Response<Boolean>> = flow {


        emit(Response.Loading)
        try {
            val userId = firebaseAuth.currentUser?.uid ?: throw Exception("User not authenticated")

            val teacherDoc = firestore.collection(NetworkConstant.COLLECTION_NAME_STUDENTS)
                .document(userId)

            // Verify document exists in single operation
            val snapshot = teacherDoc.get().await()
            if (!snapshot.exists()) {
                throw Exception("Teacher profile not found")
            }

            // Build update data
            val updateData = buildMap<String, Any> {
                //  put("updatedAt", System.currentTimeMillis())
                put("fullName", student.fullName ?: throw Exception("Full name is required"))
//                student.studyMajor?.let { put("studyMajor", it) }
//                student.favouriteTopic?.let { put("favouriteTopic", it) }
            }

            teacherDoc.update(updateData).await()
            emit(Response.Success(true))

        } catch (e: Exception) {
            emit(Response.Error(e.message ?: "Profile update failed"))
        }

    }
}