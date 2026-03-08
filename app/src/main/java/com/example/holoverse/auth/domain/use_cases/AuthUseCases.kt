package com.example.holoverse.auth.domain.use_cases

data class AuthUseCases(
    val firebaseSignUp: FirebaseSignUp,
    val firebaseSignIn: FirebaseSignIn,
    val getCurrentUser: GetCurrentUser
)
