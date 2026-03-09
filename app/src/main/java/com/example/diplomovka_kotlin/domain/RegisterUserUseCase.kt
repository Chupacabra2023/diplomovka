package com.example.diplomovka_kotlin.domain

import com.example.diplomovka_kotlin.data.services.AuthService
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser

class RegisterUserUseCase(private val authService: AuthService) {

    suspend fun execute(email: String, password: String): FirebaseUser? {
        return try {
            authService.registerWithEmail(email, password)
        } catch (e: FirebaseAuthException) {
            throw e
        }
    }
}
