package com.example.diplomovka_kotlin.data.models

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.Serializable
import java.util.Date

data class Event(
    val id: String,
    val creatorId: String = "",
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: Date,
    val place: String,
    val description: String = "",
    val dateFrom: Date? = null,
    val dateTo: Date? = null,
    val price: Double = 0.0,
    val participants: Int = 0,
    val visibility: String = "public",
    val password: String = "",
    val category: String = "",
    val subcategory: String = "",
    val attendees: List<String> = emptyList(),
    val waitlist: List<String> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val rating: Double = 0.0,
    val totalRatings: Int = 0,
    val ratedBy: List<String> = emptyList(),
    val bannedUsers: List<String> = emptyList()
) : Serializable {

    fun toMarkerOptions(icon: BitmapDescriptor? = null): MarkerOptions =
        MarkerOptions()
            .position(LatLng(latitude, longitude))
            .title(title)
            .apply { icon?.let { icon(it) } }

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id, "creatorId" to creatorId, "title" to title,
        "latitude" to latitude, "longitude" to longitude,
        "createdAt" to createdAt.time, "place" to place,
        "description" to description,
        "dateFrom" to dateFrom?.time, "dateTo" to dateTo?.time,
        "price" to price, "participants" to participants,
        "visibility" to visibility, "password" to password, "category" to category,
        "subcategory" to subcategory, "attendees" to attendees,
        "waitlist" to waitlist, "imageUrls" to imageUrls,
        "rating" to rating, "totalRatings" to totalRatings,
        "ratedBy" to ratedBy, "bannedUsers" to bannedUsers
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Event = Event(
            id = map["id"] as String,
            creatorId = map["creatorId"] as? String ?: "",
            title = map["title"] as String,
            latitude = (map["latitude"] as Number).toDouble(),
            longitude = (map["longitude"] as Number).toDouble(),
            createdAt = Date(map["createdAt"] as Long),
            place = map["place"] as String,
            description = map["description"] as? String ?: "",
            dateFrom = (map["dateFrom"] as? Long)?.let { Date(it) },
            dateTo = (map["dateTo"] as? Long)?.let { Date(it) },
            price = (map["price"] as? Number)?.toDouble() ?: 0.0,
            participants = (map["participants"] as? Number)?.toInt() ?: 0,
            visibility = map["visibility"] as? String ?: "public",
            password = map["password"] as? String ?: "",
            category = map["category"] as? String ?: "",
            subcategory = map["subcategory"] as? String ?: "",
            attendees = (map["attendees"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            waitlist  = (map["waitlist"]  as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            // backward compat: staré eventy mali imageUrl (String), nové majú imageUrls (List)
            imageUrls = (map["imageUrls"] as? List<*>)?.filterIsInstance<String>()?.takeIf { it.isNotEmpty() }
                ?: (map["imageUrl"] as? String)?.takeIf { it.isNotEmpty() }?.let { listOf(it) }
                ?: emptyList(),
            rating = (map["rating"] as? Number)?.toDouble() ?: 0.0,
            totalRatings = (map["totalRatings"] as? Number)?.toInt() ?: 0,
            ratedBy = (map["ratedBy"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            bannedUsers = (map["bannedUsers"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        )

        fun fromFirestore(id: String, data: Map<String, Any?>): Event =
            fromMap(data + mapOf("id" to id))
    }
}