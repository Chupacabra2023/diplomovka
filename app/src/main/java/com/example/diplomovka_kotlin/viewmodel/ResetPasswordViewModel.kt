package com.example.diplomovka_kotlin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.diplomovka_kotlin.data.services.AuthService
import com.example.diplomovka_kotlin.domain.ResetPasswordUseCase
import com.google.firebase.auth.FirebaseAuthException

class ResetPasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val authService = AuthService(application)
    private val resetUseCase = ResetPasswordUseCase(authService)

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    suspend fun sendResetEmail(email: String): Boolean {
        if (_isLoading.value == true) return false
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
