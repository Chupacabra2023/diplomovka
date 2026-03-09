package com.example.diplomovka_kotlin.data.models

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.Serializable
import java.util.Date

data class Event(
    val id: String,
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
    val category: String = ""
) : Serializable {

    fun toMarkerOptions(): MarkerOptions =
        MarkerOptions().position(LatLng(latitude, longitude)).title(title)

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id, "title" to title,
        "latitude" to latitude, "longitude" to longitude,
        "createdAt" to createdAt.time, "place" to place,
        "description" to description,
        "dateFrom" to dateFrom?.time, "dateTo" to dateTo?.time,
        "price" to price, "participants" to participants,
        "visibility" to visibility, "category" to category
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Event = Event(
            id = map["id"] as String,
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
            category = map["category"] as? String ?: ""
        )

        fun fromFirestore(id: String, data: Map<String, Any?>): Event =
            fromMap(data + mapOf("id" to id))
    }
}