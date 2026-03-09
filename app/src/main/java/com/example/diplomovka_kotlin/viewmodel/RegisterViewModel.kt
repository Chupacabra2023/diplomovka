package com.example.diplomovka_kotlin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.diplomovka_kotlin.data.services.AuthService
import com.example.diplomovka_kotlin.domain.RegisterUserUseCase
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val authService = AuthService(application)
    private val registerUseCase = RegisterUserUseCase(authService)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun register(email: String, password: String): Boolean {
        _isLoading.value = true
        return try {
            registerUseCase.execute(email, password)
            true
        } catch (e: FirebaseAuthException) {
            _error.value = when (e.errorCode) {
                "email-already-in-use" -> "Tento e-mail je už zaregistrovaný."
                "invalid-email"        -> "Neplatný formát e-mailu."
                "weak-password"        -> "Heslo je príliš slabé."
                else -> "Chyba pri registrácii: ${e.message ?: e.errorCode}"
            }
            false
        } finally {
            _isLoading.value = false
        }
    }

    fun clearError() { _error.value = null }
}
