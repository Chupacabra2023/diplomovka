package com.example.diplomovka_kotlin.data.models

data class UserProfile(
    val uid: String,
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val bio: String = "",
    val preferredCategories: List<String> = emptyList(),
    val favoriteEventIds: List<String> = emptyList()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "displayName" to displayName,
        "email" to email,
        "photoUrl" to photoUrl,
        "bio" to bio,
        "preferredCategories" to preferredCategories,
        "favoriteEventIds" to favoriteEventIds
    )

    companion object {
        fun fromFirestore(uid: String, data: Map<String, Any?>): UserProfile = UserProfile(
            uid = uid,
            displayName = data["displayName"] as? String ?: "",
            email = data["email"] as? String ?: "",
            photoUrl = data["photoUrl"] as? String ?: "",
            bio = data["bio"] as? String ?: "",
            preferredCategories = (data["preferredCategories"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            favoriteEventIds = (data["favoriteEventIds"] as? List<String>)?.filterIsInstance<String>() ?: emptyList()
        )
    }
}
