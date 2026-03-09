package com.example.diplomovka_kotlin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.diplomovka_kotlin.data.services.AuthService
import com.example.diplomovka_kotlin.domain.ResetPasswordUseCase
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ResetPasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val authService = AuthService(application)
    private val resetUseCase = ResetPasswordUseCase(authService)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun sendResetEmail(email: String): Boolean {
        if (_isLoading.value) return false
        _isLoading.value = true
        _error.value = null

        return try {
            resetUseCase.execute(email)
            true
        } catch (e: FirebaseAuthException) {
            _error.value = when (e.errorCode) {
                "user-not-found" -> "Používateľ s týmto e-mailom neexistuje."
                "invalid-email"  -> "Neplatný formát e-mailu."
                else -> "Chyba: ${e.message ?: e.errorCode}"
            }
            false
        } catch (e: Exception) {
            _error.value = "Nastala neočakávaná chyba pri odosielaní. Skúste znova."
            false
        } finally {
            _isLoading.value = false
        }
    }

    fun clearError() { _error.value = null }
}
