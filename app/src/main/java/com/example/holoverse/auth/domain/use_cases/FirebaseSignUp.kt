package com.example.holoverse.auth.domain.use_cases

import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.repositiory.AuthRepository
import javax.inject.Inject

class FirebaseSignUp @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
      userDto: User,
        password: String
    ) =repository.firebaseSignUp(userDto, password)

}