package com.example.diplomovka_kotlin.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.diplomovka_kotlin.data.services.AuthService
import com.example.diplomovka_kotlin.domain.LoginUserUseCase
import com.google.firebase.auth.FirebaseAuthException

class LoginViewModel(private val context: Context) : ViewModel() {

    private val authService = AuthService(context)
    private val loginUseCase = LoginUserUseCase(authService)

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    suspend fun login(email: String, password: String): Boolean {
        if (_isLoading.value == true) return false
        _isLoading.value = true
        _error.value = null

        return try {
            loginUseCase.execute(email, password)
            true
        } catch (e: FirebaseAuthException) {
            _error.value = when (e.errorCode) {
                "user-not-found", "wrong-password" -> "Neplatný e-mail alebo heslo."
                "invalid-email"                    -> "Neplatný formát e-mailu."
                "user-disabled"                    -> "Tento účet bol deaktivovaný."
                "email-not-verified"               -> {
                    authService.sendEmailVerification()
                    e.message
                }
                else -> "Chyba pri prihlásení: ${e.message ?: e.errorCode}"
            }
            false
        } catch (e: Exception) {
            _error.value = "Nastala neočakávaná chyba. Skúste to znova."
            false
        } finally {
            _isLoading.value = false
        }
    }

    fun clearError() { _error.value = null }
}
