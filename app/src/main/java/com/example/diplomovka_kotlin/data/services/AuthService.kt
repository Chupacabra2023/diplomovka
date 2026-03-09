package com.example.diplomovka_kotlin.data.services

import android.content.Context
import com.example.diplomovka_kotlin.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthService(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun registerWithEmail(email: String, password: String): FirebaseUser? {
        val credential = auth.createUserWithEmailAndPassword(email, password).await()
        sendEmailVerification()
        return credential.user
    }

    suspend fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    suspend fun sendEmailVerification() {
        val user = auth.currentUser
        if (user != null && !user.isEmailVerified) {
            user.sendEmailVerification().await()
        }
    }

    fun getGoogleSignInIntent() = googleSignInClient.signInIntent

    suspend fun firebaseAuthWithGoogle(idToken: String): FirebaseUser? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        return result.user
    }

    suspend fun logout() {
        auth.signOut()
        googleSignInClient.signOut().await()
    }
}