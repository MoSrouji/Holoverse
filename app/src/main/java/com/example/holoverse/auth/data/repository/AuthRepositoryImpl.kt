package com.example.holoverse.auth.data.repository

import android.util.Log
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repository.AuthRepository
import com.example.holoverse.core.utils.NetworkConstant.COLLECTION_NAME_USERS
import com.example.holoverse.core.utils.NetworkConstant.COLLECTION_NAME_TRANSACTIONS
import com.example.holoverse.core.utils.PreferenceManager
import com.example.holoverse.core.utils.Response
import com.example.holoverse.payment.domain.model.TransactionType
import com.example.holoverse.payment.domain.repository.PaymentRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val preferenceManager: PreferenceManager,
    private val fetchDataRepository: com.example.holoverse.fetch.domain.FetchDataRepository,
    private val paymentRepository: PaymentRepository
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

            val savedUser = when (userDto) {
                is User.Student -> {
                    val userDoc = firestore.collection(COLLECTION_NAME_USERS)
                        .document(userId)

                    if (userDoc.get().await().exists()) {
                        // Clean up auth user if document exists
                        user.delete().await()
                        throw Exception("Sign up failed ")
                    }
                    val student = userDto.copy(
                        userId = userId,
                        createdAt = System.currentTimeMillis()
                    )
                    userDoc.set(student).await()
                    student
                }

                is User.Mentor -> {
                    val userDoc = firestore.collection(COLLECTION_NAME_USERS)
                        .document(userId)
                    if (userDoc.get().await().exists()) {
                        // Clean up auth user if document exists
                        user.delete().await()
                        throw Exception("Sign up failed ")
                    }

                    val mentor = userDto.copy(
                        userId = userId,
                        createdAt = System.currentTimeMillis()
                    )
                    userDoc.set(mentor).await()

                    // Mentor Signup Fee ($10)
                    try {
                        val adminSnapshot = firestore.collection(COLLECTION_NAME_USERS)
                            .whereEqualTo("accountType", "Admin")
                            .limit(1)
                            .get()
                            .await()
                        val adminId = adminSnapshot.documents.firstOrNull()?.id
                        if (adminId != null) {
                            paymentRepository.transferFunds(
                                senderId = userId,
                                receiverId = adminId,
                                amount = 10.0,
                                type = TransactionType.MENTOR_SIGNUP,
                                metadata = mapOf("mentorName" to (mentor.fullName ?: "Unknown"))
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("AuthRepository", "Failed to process signup fee: ${e.message}")
                    }

                    mentor
                }

                is User.Admin -> throw Exception("Admin sign up is not allowed")
            }

            preferenceManager.saveUser(savedUser, isProfileComplete = false)
            emit(Response.Success(true))

        } catch (e: Exception) {
            if (e is CancellationException) throw e
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
            if (e is CancellationException) throw e
            emit(Response.Error(e.message ?: "Sign in failed"))
        }


    }

    override suspend fun firebaseSignOut(): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            preferenceManager.clearData()
            firebaseAuth.signOut()
            fetchDataRepository.clearCache()
            emit(Response.Success(true))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.localizedMessage ?: "Sign out failed"))
        }
    }

    override suspend fun updateMentorProfile(mentor: User.Mentor): Flow<Response<Boolean>> =
        flow {
            emit(Response.Loading)
            try {
                val userId =
                    firebaseAuth.currentUser?.uid ?: throw Exception("User not authenticated")

                val teacherDoc = firestore.collection(COLLECTION_NAME_USERS)
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
                    mentor.profileImageUrl?.let { put("profileImageUrl", it) }
                }

                teacherDoc.update(updateData).await()
                Log.d("AuthRepository", "Firestore update successful for Mentor. Data: $updateData")

                // Refresh local cache
                val updatedUser = getCurrentUser()
                Log.d("AuthRepository", "Fetched updated user from Firestore: $updatedUser")
                updatedUser?.let {
                    preferenceManager.saveUser(it)
                    Log.d("AuthRepository", "Saved updated user to PreferenceManager")
                }

                emit(Response.Success(true))

            } catch (e: Exception) {
                if (e is CancellationException) throw e
                emit(Response.Error(e.message ?: "Profile update failed"))
            }
        }

    override suspend fun updateStudentProfile(student: User.Student): Flow<Response<Boolean>> =
        flow {
            emit(Response.Loading)
            try {
                val userId =
                    firebaseAuth.currentUser?.uid ?: throw Exception("User not authenticated")

                val studentDoc = firestore.collection(COLLECTION_NAME_USERS)
                    .document(userId)

                val snapshot = studentDoc.get().await()
                if (!snapshot.exists()) {
                    throw Exception("Student profile not found")
                }

                val updateData = buildMap<String, Any> {
                    student.fullName?.let { put("fullName", it) }
                    student.phoneNumber?.let { put("phoneNumber", it) }
                    student.profileImageUrl?.let { put("profileImageUrl", it) }
                    student.dateOfBirth?.let { put("dateOfBirth", it) }
                    student.address?.let { put("address", it) }
                    student.gender?.let { put("gender", it) }
                    student.currentGradeLevel?.let { put("currentGradeLevel", it) }
                    student.universityName?.let { put("universityName", it) }
                    student.faculty?.let { put("faculty", it) }
                    student.academicInterests?.let { put("academicInterests", it) }
                    student.preferredLearningTime?.let { put("preferredLearningTime", it) }
                }

                studentDoc.update(updateData).await()
                Log.d(
                    "AuthRepository",
                    "Firestore update successful for Student. Data: $updateData"
                )

                // Refresh local cache
                val updatedUser = getCurrentUser()
                Log.d("AuthRepository", "Fetched updated user from Firestore: $updatedUser")
                updatedUser?.let {
                    preferenceManager.saveUser(it)
                    Log.d("AuthRepository", "Saved updated user to PreferenceManager")
                }

                emit(Response.Success(true))

            } catch (e: Exception) {
                if (e is CancellationException) throw e
                emit(Response.Error(e.message ?: "Profile update failed"))
            }

        }

    override suspend fun updateEmail(newEmail: String): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            val user = firebaseAuth.currentUser ?: throw Exception("User not authenticated")
            val userId = user.uid
            user.updateEmail(newEmail).await()
            
            // Also update in Firestore
            val userRef = firestore.collection(COLLECTION_NAME_USERS).document(userId)
            userRef.update("email", newEmail).await()
            
            // Refresh local cache
            val updatedUser = getCurrentUser()
            updatedUser?.let { preferenceManager.saveUser(it) }
            
            emit(Response.Success(true))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.message ?: "Failed to update email"))
        }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            val user = firebaseAuth.currentUser ?: throw Exception("User not authenticated")
            val email = user.email ?: throw Exception("User email not found")
            
            // Re-authenticate
            val credential = EmailAuthProvider.getCredential(email, oldPassword)
            user.reauthenticate(credential).await()
            
            // Update password
            user.updatePassword(newPassword).await()
            
            emit(Response.Success(true))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.message ?: "Failed to change password"))
        }
    }

    override suspend fun getCurrentUser(): User? {
        val currentUser = firebaseAuth.currentUser ?: return null
        val uid = currentUser.uid

        return try {
            val userDoc = firestore.collection(COLLECTION_NAME_USERS).document(uid).get().await()
            if (!userDoc.exists()) return null

            val typeString = userDoc.getString("accountType")
            when (typeString) {
                "Student" -> userDoc.toObject(User.Student::class.java)?.copy(userId = uid)
                "Mentor" -> {
                    val mentor = userDoc.toObject(User.Mentor::class.java)
                    val specString = userDoc.getString("specialization")
                    mentor?.copy(
                        userId = uid,
                        specialization = if (specString != null) com.example.holoverse.core.domain.model.AppCategory.fromString(specString) else mentor.specialization
                    )
                }
                "Admin" -> userDoc.toObject(User.Admin::class.java)?.copy(userId = uid)
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getCachedUser(): User? {
        return preferenceManager.getUser()
    }


    override suspend fun updateUser(user: User?, newEmail: String?): Boolean {
        // Implementation for general update logic if needed
        return true
    }

    override suspend fun updateFcmToken(token: String): Response<Boolean> {
        return try {
            val userId =
                firebaseAuth.currentUser?.uid ?: return Response.Error("User not authenticated")

            firestore.collection(COLLECTION_NAME_USERS).document(userId).update("fcmToken", token).await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Failed to update FCM token")
        }
    }

    override suspend fun getFcmToken(userId: String): String? {
        return try {
            firestore.collection(COLLECTION_NAME_USERS).document(userId).get().await().getString("fcmToken")
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun followMentor(followerId: String, mentorId: String): Response<Boolean> {
        return try {
            val followerRef = firestore.collection(COLLECTION_NAME_USERS).document(followerId)
            val mentorRef = firestore.collection(COLLECTION_NAME_USERS).document(mentorId)

            firestore.runBatch { batch ->
                // Update Follower
                batch.update(followerRef, "following", FieldValue.arrayUnion(mentorId))
                batch.update(followerRef, "followingCount", FieldValue.increment(1))

                // Update Mentor
                batch.update(mentorRef, "followers", FieldValue.arrayUnion(followerId))
                batch.update(mentorRef, "followersCount", FieldValue.increment(1))
            }.await()

            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Failed to follow mentor")
        }
    }

    override suspend fun unfollowMentor(followerId: String, mentorId: String): Response<Boolean> {
        return try {
            val followerRef = firestore.collection(COLLECTION_NAME_USERS).document(followerId)
            val mentorRef = firestore.collection(COLLECTION_NAME_USERS).document(mentorId)

            firestore.runBatch { batch ->
                // Update Follower
                batch.update(followerRef, "following", FieldValue.arrayRemove(mentorId))
                batch.update(followerRef, "followingCount", FieldValue.increment(-1))

                // Update Mentor
                batch.update(mentorRef, "followers", FieldValue.arrayRemove(followerId))
                batch.update(mentorRef, "followersCount", FieldValue.increment(-1))
            }.await()

            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Failed to unfollow mentor")
        }
    }

    override suspend fun isFollowing(followerId: String, mentorId: String): Boolean {
        return try {
            val followerDoc = firestore.collection(COLLECTION_NAME_USERS).document(followerId).get().await()
            val followingList = followerDoc.get("following") as? List<*>
            followingList?.contains(mentorId) == true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun enrollInCourse(userId: String, courseId: String, instructorId: String): Response<Boolean> {
        return try {
            val userRef = firestore.collection(COLLECTION_NAME_USERS).document(userId)
            val courseRef = firestore.collection("courses").document(courseId)
            val mentorRef = firestore.collection(COLLECTION_NAME_USERS).document(instructorId)

            firestore.runBatch { batch ->
                // Update User
                batch.update(userRef, "enrolledCourses", FieldValue.arrayUnion(courseId))
                batch.update(userRef, "currentCourses", FieldValue.arrayUnion(courseId))
                
                // Update Course stats
                batch.update(courseRef, "numEnrolled", FieldValue.increment(1))
                
                // Update Mentor stats
                batch.update(mentorRef, "totalStudentsTaught", FieldValue.increment(1))
            }.await()

            // Refresh local cache
            val updatedUser = getCurrentUser()
            updatedUser?.let { preferenceManager.saveUser(it) }

            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Enrollment failed")
        }
    }

    override suspend fun toggleSaveCourse(userId: String, courseId: String): Response<Boolean> {
        return try {
            val user =
                getCachedUser() ?: getCurrentUser() ?: throw Exception("User not authenticated")
            val userRef = firestore.collection(COLLECTION_NAME_USERS).document(userId)

            val isSaved = when (user) {
                is User.Student -> user.savedCourses?.contains(courseId) == true
                is User.Mentor -> user.savedCourses?.contains(courseId) == true
                else -> false
            }

            if (isSaved) {
                userRef.update("savedCourses", FieldValue.arrayRemove(courseId)).await()
            } else {
                userRef.update("savedCourses", FieldValue.arrayUnion(courseId)).await()
            }

            // Refresh local cache
            val updatedUser = getCurrentUser()
            updatedUser?.let { preferenceManager.saveUser(it) }

            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Failed to update saved courses")
        }
    }

    override suspend fun addCourseToMentor(mentorId: String, courseId: String): Response<Boolean> {
        return try {
            val mentorRef = firestore.collection(COLLECTION_NAME_USERS).document(mentorId)
            mentorRef.update("coursesCreated", FieldValue.arrayUnion(courseId)).await()
            
            // Refresh local cache if the current user is this mentor
            val currentUser = getCachedUser()
            if (currentUser?.userId == mentorId) {
                val updatedUser = getCurrentUser()
                updatedUser?.let { preferenceManager.saveUser(it) }
            }
            
            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Failed to add course to mentor")
        }
    }

    override fun getSupportAdmin(): Flow<Response<User.Admin>> = flow {
        emit(Response.Loading)
        try {
            val snapshot = firestore.collection(COLLECTION_NAME_USERS)
                .whereEqualTo("accountType", "Admin")
                .limit(1)
                .get()
                .await()
            val admin = snapshot.documents.firstOrNull()?.toObject(User.Admin::class.java)
            if (admin != null) {
                emit(Response.Success(admin))
            } else {
                emit(Response.Error("No support admin available"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.message ?: "Failed to get support admin"))
        }
    }

    override suspend fun upgradeToMentor(userId: String): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            val userRef = firestore.collection(COLLECTION_NAME_USERS).document(userId)
            
            // Find Admin
            val adminSnapshot = firestore.collection(COLLECTION_NAME_USERS)
                .whereEqualTo("accountType", "Admin")
                .limit(1)
                .get()
                .await()
            
            val adminId = adminSnapshot.documents.firstOrNull()?.id ?: "admin_default_id"
            val adminRef = firestore.collection(COLLECTION_NAME_USERS).document(adminId)

            firestore.runTransaction { transaction ->
                val userDoc = transaction.get(userRef)
                val adminDoc = transaction.get(adminRef)

                if (!userDoc.exists()) throw Exception("User not found")
                if (userDoc.getString("accountType") != "Student") throw Exception("Only students can upgrade to mentor")
                
                val walletBalance = userDoc.getDouble("walletBalance") ?: 0.0
                if (walletBalance < 10.0) throw Exception("Insufficient balance. $10 required.")

                val adminBalance = adminDoc.getDouble("walletBalance") ?: 0.0

                // 1. Transfer $10 to Admin
                transaction.update(userRef, "walletBalance", walletBalance - 10.0)
                transaction.update(adminRef, "walletBalance", adminBalance + 10.0)

                // 2. Update User to Mentor
                val mentorData = mapOf(
                    "accountType" to "Mentor",
                    "specialization" to com.example.holoverse.core.domain.model.AppCategory.OTHER.name,
                    "followersCount" to 0,
                    "totalStudentsTaught" to 0,
                    "averageRating" to 0.0,
                    "reviewsCount" to 0
                )
                transaction.update(userRef, mentorData)

                // 3. Create Transaction Record
                val transactionId = UUID.randomUUID().toString()
                val paymentTransaction = com.example.holoverse.payment.domain.model.Transaction(
                    id = transactionId,
                    senderId = userId,
                    receiverId = adminId,
                    amount = 10.0,
                    type = TransactionType.MENTOR_UPGRADE,
                    timestamp = System.currentTimeMillis(),
                    metadata = mapOf("reason" to "Student to Mentor Upgrade")
                )
                
                val transRef = firestore.collection(COLLECTION_NAME_TRANSACTIONS).document(transactionId)
                transaction.set(transRef, paymentTransaction)
            }.await()

            // 4. Update Local Cache
            val updatedUser = getCurrentUser()
            updatedUser?.let { preferenceManager.saveUser(it) }

            emit(Response.Success(true))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.message ?: "Upgrade failed"))
        }
    }
}

