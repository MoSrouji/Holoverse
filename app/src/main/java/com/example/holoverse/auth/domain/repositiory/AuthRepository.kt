package com.example.holoverse.auth.domain.repositiory

import com.example.holoverse.auth.domain.entities.Student
import com.example.holoverse.auth.domain.entities.Teacher
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.utils.Response
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    suspend fun firebaseSignUp(
        userDto: User,
        password: String

    ): Flow<Response<Boolean>>

    suspend fun firebaseSignIn(
        email: String,
        password: String
    ): Flow<Response<Boolean>>

    suspend fun firebaseSignOut(): Flow<Response<Boolean>>
    suspend  fun updateTeacherProfile(teacher: Teacher):Flow<Response<Boolean>>
    suspend  fun updateStudentProfile(student: Student):Flow<Response<Boolean>>

}