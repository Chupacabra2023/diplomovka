package com.example.diplomovka_kotlin.data.services

import com.example.diplomovka_kotlin.data.models.Event
import java.util.Date
import kotlin.math.*

data class FilterCriteria(
    val name: String? = null,
    val category: String? = null,
    val subcategory: String? = null,
    val participants: Int? = null,
    val maxPrice: Double? = null,
    val visibility: String? = null,
    val dateFrom: Date? = null,
    val dateTo: Date? = null,
    val maxDistanceKm: Double? = null,
    val refLat: Double = 0.0,
    val refLng: Double = 0.0
)

class EventFilterService(private val criteria: FilterCriteria) {

    fun filter(allEvents: List<Event>): List<Event> = allEvents.filter { matches(it) }

    private fun matches(event: Event): Boolean {
        if (!criteria.name.isNullOrEmpty() &&
            !event.title.lowercase().contains(criteria.name.lowercase())) return false

        if (criteria.visibility != null && criteria.visibility != "All" &&
            event.visibility != criteria.visibility) return false

        if (criteria.participants != null && criteria.participants > 0 &&
            event.participants < criteria.participants) return false

        if (criteria.maxPrice != null && criteria.maxPrice < 500.0 &&
            event.price > criteria.maxPrice) return false

        if (!criteria.category.isNullOrEmpty() &&
            event.category != criteria.category) return false

        if (!criteria.subcategory.isNullOrEmpty() &&
            event.subcategory != criteria.subcategory) return false

        if (criteria.dateFrom != null && event.dateFrom != null &&
            event.dateFrom.before(criteria.dateFrom)) return false

        if (criteria.dateTo != null && event.dateTo != null &&
            event.dateTo.after(criteria.dateTo)) return false

        if (criteria.maxDistanceKm != null) {
            val dist = haversineKm(criteria.refLat, criteria.refLng, event.latitude, event.longitude)
            if (dist > criteria.maxDistanceKm) return false
        }

        return true
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return 6371.0 * 2 * asin(sqrt(a))
    }
}
