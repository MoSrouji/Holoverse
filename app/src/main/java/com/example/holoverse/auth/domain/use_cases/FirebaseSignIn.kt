package com.example.holoverse.auth.domain.use_cases

import com.example.holoverse.auth.domain.repositiory.AuthRepository
import javax.inject.Inject

class FirebaseSignIn @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String , password: String) =
        repository.firebaseSignIn(email=email ,password =password)
}