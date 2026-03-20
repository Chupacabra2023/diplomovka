package com.example.diplomovka_kotlin.recommendation

import com.example.diplomovka_kotlin.data.models.Event
import java.util.Date
import kotlin.math.*

object RecommendationEngine {

    private const val MAIN_CATEGORY_WEIGHT = 0.20
    private const val SUB_CATEGORY_WEIGHT = 0.16
    private const val DISTANCE_WEIGHT = 0.16
    private const val RATING_WEIGHT = 0.16
    private const val POPULARITY_WEIGHT = 0.16
    private const val FAVORITE_SUBCATEGORY_BONUS = 0.16

    /**
     * Returns scored + sorted recommendations for the current user.
     *
     * @param userId            current user's UID
     * @param userLat / userLng current user location (used for distance scoring)
     * @param allEvents         full in-memory events map
     * @param visitedEventIds   event IDs the user has already attended
     * @param favoriteEventIds  event IDs the user has favorited
     * @param maxDistanceKm     hard distance filter (default 50 km)
     * @param count             max results to return (default 20)
     */
    fun getRecommendations(
        userId: String,
        userLat: Double,
        userLng: Double,
        allEvents: Collection<Event>,
        visitedEventIds: List<String>,
        favoriteEventIds: List<String>,
        maxDistanceKm: Double = 50.0,
        count: Int = 20
    ): List<ScoredEvent> {
        val userProfile = buildUserProfile(visitedEventIds, allEvents)

        val favoriteSubCategories = allEvents
            .filter { it.id in favoriteEventIds }
            .map { it.category }
            .toSet()

        val now = Date()

        return allEvents
            .filter { event ->
                event.id !in visitedEventIds
                    && event.creatorId != userId
                    && event.visibility == "public"
                    && (event.dateFrom?.after(now) == true || event.dateFrom == null)
                    && (event.participants == 0 || event.attendees.size < event.participants)
            }
            .map { event ->
                scoreEvent(event, userLat, userLng, userProfile, favoriteSubCategories)
            }
            .filter { scored ->
                val dist = scored.scoreBreakdown["distance_km"] ?: 999.0
                dist <= maxDistanceKm
            }
            .sortedByDescending { it.score }
            .take(count)
    }

    // ── Build preference profile from visited events ──────────────────────────

    private fun buildUserProfile(
        visitedEventIds: List<String>,
        allEvents: Collection<Event>
    ): Map<String, Double> {
        if (visitedEventIds.isEmpty()) return emptyMap()

        val mainCounts = mutableMapOf<String, Int>()
        val subCounts = mutableMapOf<String, Int>()
        var total = 0

        for (eventId in visitedEventIds) {
            val event = allEvents.find { it.id == eventId } ?: continue
            val main = CategoryMapper.getMainCategory(event.category)
            mainCounts[main] = (mainCounts[main] ?: 0) + 1
            subCounts[event.category] = (subCounts[event.category] ?: 0) + 1
            total++
        }

        if (total == 0) return emptyMap()

        val prefs = mutableMapOf<String, Double>()
        mainCounts.forEach { (cat, cnt) -> prefs[cat] = cnt.toDouble() / total }
        subCounts.forEach { (sub, cnt) -> prefs[sub] = cnt.toDouble() / total }
        return prefs
    }

    // ── Score a single event ──────────────────────────────────────────────────

    private fun scoreEvent(
        event: Event,
        userLat: Double,
        userLng: Double,
        userProfile: Map<String, Double>,
        favoriteSubCategories: Set<String>
    ): ScoredEvent {
        val breakdown = mutableMapOf<String, Double>()
        val mainCategory = CategoryMapper.getMainCategory(event.category)
        val subCategory = event.category

        // 1. Main category
        var mainMatch = userProfile[mainCategory] ?: 0.0
        if (userProfile.isEmpty()) mainMatch = 0.5
        else if (mainMatch == 0.0) mainMatch = 0.05
        val mainScore = mainMatch * MAIN_CATEGORY_WEIGHT
        breakdown["main_category"] = mainScore
        breakdown["main_category_match"] = mainMatch

        // 2. Sub category
        val subMatch = userProfile[subCategory] ?: 0.0
        val subScore = subMatch * SUB_CATEGORY_WEIGHT
        breakdown["sub_category"] = subScore
        breakdown["sub_category_match"] = subMatch

        // 3. Distance
        val distance = haversineKm(userLat, userLng, event.latitude, event.longitude)
        val distScore = (1.0 / (1.0 + distance / 10.0)) * DISTANCE_WEIGHT
        breakdown["distance"] = distScore
        breakdown["distance_km"] = distance

        // 4. Rating
        val ratingScore = if (event.totalRatings > 0) {
            (event.rating / 5.0) * RATING_WEIGHT
        } else {
            0.5 * RATING_WEIGHT
        }
        breakdown["rating"] = ratingScore
        breakdown["rating_value"] = event.rating

        // 5. Popularity
        val maxCap = if (event.participants > 0) event.participants.toDouble() else 100.0
        val popScore = min(event.attendees.size.toDouble() / maxCap, 1.0) * POPULARITY_WEIGHT
        breakdown["popularity"] = popScore
        breakdown["attendees_count"] = event.attendees.size.toDouble()

        // 6. Favorite subcategory bonus
        val favBonus = if (favoriteSubCategories.contains(subCategory)) FAVORITE_SUBCATEGORY_BONUS else 0.0
        if (favBonus > 0) breakdown["favorite_bonus"] = favBonus

        val totalScore = mainScore + subScore + distScore + ratingScore + popScore + favBonus
        return ScoredEvent(event, totalScore, breakdown)
    }

    // ── Haversine distance formula ────────────────────────────────────────────

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
