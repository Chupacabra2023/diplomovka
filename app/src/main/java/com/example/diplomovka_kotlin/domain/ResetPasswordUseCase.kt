package com.example.diplomovka_kotlin.domain

import com.example.diplomovka_kotlin.data.services.AuthService

class ResetPasswordUseCase(private val authService: AuthService) {

    suspend fun execute(email: String) {
        try {
            authService.sendPasswordResetEmail(email)
        } catch (e: Exception) {
            throw e
        }
    }
}
