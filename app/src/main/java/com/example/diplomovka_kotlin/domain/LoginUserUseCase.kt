package com.example.diplomovka_kotlin.domain

import com.example.diplomovka_kotlin.data.services.AuthService
import com.google.firebase.auth.FirebaseAuthException

class LoginUserUseCase(private val authService: AuthService) {

    suspend fun execute(email: String, password: String) {
        authService.signInWithEmail(email, password)

        val user = authService.currentUser
        if (user != null && !user.isEmailVerified) {
            authService.logout()
            throw FirebaseAuthException(
                "email-not-verified",
                "E-mail nebol overený. Skontrolujte svoju schránku a kliknite na overovací odkaz."
            )
        }
    }
}
