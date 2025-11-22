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

    ): AuthRepository {
    override suspend fun firebaseSignUp(
        userDto: User,
        password: String
    ): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(userDto.email!!, password).await()
            val user = authResult.user ?: throw Exception("User creation failed")

            val userId = user.uid

            when (userDto.accountType){
                UserType.Student ->{
                    val userDoc = firestore.collection(NetworkConstant.COLLECTION_NAME_STUDENTS )
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

                UserType.Teacher ->{
                    val userDoc = firestore.collection(NetworkConstant.COLLECTION_NAME_TEACHERS )
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

        }
        catch (e: Exception){
            emit(Response.Error(e.message ?: "Sign up failed"))
        }
    }

    override suspend fun firebaseSignIn(
        email: String,
        password: String
    ): Flow<Response<Boolean>> =flow {
        emit(Response.Loading)

        try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()

            // Additional checks if needed
            if (authResult.user != null) {
                emit(Response.Success(true))
            } else {
                emit(Response.Error("Sign in failed - no user returned"))
            }
        }
        catch (e: Exception){
            emit(Response.Error(e.message ?: "Sign in failed"))
        }


    }

    override suspend fun firebaseSignOut(): Flow<Response<Boolean>> =flow {
        emit(Response.Loading)
        try {
            firebaseAuth.signOut()
            emit(Response.Success(true))
        } catch (e: Exception) {
            emit(Response.Error(e.localizedMessage ?: "Sign out failed"))
        }
    }
    }
