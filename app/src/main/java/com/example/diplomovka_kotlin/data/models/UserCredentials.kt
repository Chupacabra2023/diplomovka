package com.example.diplomovka_kotlin.data.models

data class UserCredentials(
    val email: String,
    val password: String
) {
    fun isValidEmail(): Boolean = email.contains('@') && email.contains('.')
    fun isValidPassword(): Boolean = password.length >= 6
}