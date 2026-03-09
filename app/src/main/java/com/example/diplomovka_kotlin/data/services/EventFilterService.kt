package com.example.diplomovka_kotlin.data.services

import com.example.diplomovka_kotlin.data.models.Event
import java.util.Date

data class FilterCriteria(
    val name: String? = null,
    val category: String? = null,
    val participants: Int? = null,
    val maxPrice: Double? = null,
    val visibility: String? = null,
    val dateFrom: Date? = null,
    val dateTo: Date? = null
)

class EventFilterService(private val criteria: FilterCriteria) {

    fun filter(allEvents: List<Event>): List<Event> {
        return allEvents.filter { matches(it) }
    }

    private fun matches(event: Event): Boolean {

        // Názov
        if (!criteria.name.isNullOrEmpty() &&
            !event.title.lowercase().contains(criteria.name.lowercase())) {
            return false
        }

        // Viditeľnosť
        if (criteria.visibility != null &&
            criteria.visibility != "All" &&
            event.visibility != criteria.visibility) {
            return false
        }

        // Účastníci
        if (criteria.participants != null &&
            criteria.participants > 0 &&
            event.participants < criteria.participants) {
            return false
        }

        // Cena
        if (criteria.maxPrice != null && event.price > criteria.maxPrice) {
            return false
        }

        // Kategória
        if (!criteria.category.isNullOrEmpty() &&
            event.category != criteria.category) {
            return false
        }

        // Dátum OD
        if (criteria.dateFrom != null &&
            event.dateFrom != null &&
            event.dateFrom.before(criteria.dateFrom)) {
            return false
        }

        // Dátum DO
        if (criteria.dateTo != null &&
            event.dateTo != null &&
            event.dateTo.after(criteria.dateTo)) {
            return false
        }

        return true
    }
}