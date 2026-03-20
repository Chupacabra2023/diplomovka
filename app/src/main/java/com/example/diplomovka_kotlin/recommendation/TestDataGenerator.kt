package com.example.diplomovka_kotlin.recommendation

import android.util.Log
import com.example.diplomovka_kotlin.data.models.CATEGORY_MAP
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.random.Random

private const val TAG = "TestDataGenerator"

class TestDataGenerator {

    private val db = FirebaseFirestore.getInstance()
    private val rng = Random.Default

    private val bratislavaLat = 48.1486
    private val bratislavaLng = 17.1077

    private val allSubcategories: List<String> by lazy {
        CATEGORY_MAP.values.flatten()
    }

    // ── Generate events ───────────────────────────────────────────────────────

    suspend fun generateTestEvents(count: Int) {
        Log.d(TAG, "Generujem $count udalostí...")

        repeat(count) { i ->
            val eventId = "test_event_$i"

            val lat = bratislavaLat + (rng.nextDouble() - 0.5) * 0.3
            val lng = bratislavaLng + (rng.nextDouble() - 0.5) * 0.3

            val category = allSubcategories[rng.nextInt(allSubcategories.size)]
            val daysAhead = 1 + rng.nextInt(30)
            val dateFrom = Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(daysAhead.toLong()))

            val rating = 3.0 + rng.nextDouble() * 2.0          // 3.0 – 5.0
            val totalRatings = rng.nextInt(20) + 1
            val maxAttendees = 4 + rng.nextInt(97)              // 4 – 100

            val data = mapOf(
                "creatorId" to "test_creator",
                "title" to "udalost ${i + 1}",
                "latitude" to lat,
                "longitude" to lng,
                "createdAt" to System.currentTimeMillis(),
                "place" to "Bratislava",
                "description" to "",
                "dateFrom" to dateFrom.time,
                "dateTo" to null,
                "price" to 0.0,
                "participants" to maxAttendees,
                "visibility" to "public",
                "password" to "",
                "category" to category,
                "subcategory" to category,
                "attendees" to emptyList<String>(),
                "waitlist" to emptyList<String>(),
                "imageUrls" to emptyList<String>(),
                "rating" to rating,
                "totalRatings" to totalRatings
            )

            try {
                db.collection("events").document(eventId).set(data).await()
            } catch (e: Exception) {
                Log.e(TAG, "Chyba pri ukladaní eventu $eventId: ${e.message}")
            }

            if ((i + 1) % 10 == 0) Log.d(TAG, "Vytvorených ${i + 1}/$count udalostí")
        }

        Log.d(TAG, "Hotovo! Vytvorených $count udalostí")
    }

    // ── Generate users ────────────────────────────────────────────────────────

    suspend fun generateTestUsers(count: Int) {
        Log.d(TAG, "Generujem $count užívateľov...")

        // Load existing test event IDs
        val eventsSnap = db.collection("events")
            .whereGreaterThanOrEqualTo("__name__", "test_event_")
            .whereLessThan("__name__", "test_event_\uF8FF")
            .get().await()

        if (eventsSnap.isEmpty) {
            Log.w(TAG, "Žiadne test eventy! Najprv spusti generateTestEvents()")
            return
        }

        val availableIds = eventsSnap.documents.map { it.id }
        Log.d(TAG, "Našiel som ${availableIds.size} existujúcich eventov")

        var successfulAdds = 0
        var capacityReached = 0

        repeat(count) { i ->
            val userId = "test_user_$i"

            val lat = bratislavaLat + (rng.nextDouble() - 0.5) * 0.2
            val lng = bratislavaLng + (rng.nextDouble() - 0.5) * 0.2

            val visitedCount = 3 + rng.nextInt(8)
            val shuffled = availableIds.shuffled(rng)
            val visitedIds = shuffled.take(visitedCount.coerceAtMost(availableIds.size))

            val favoriteCount = rng.nextInt(6).coerceAtMost(visitedIds.size)
            val favoriteIds = visitedIds.shuffled(rng).take(favoriteCount)

            val userData = mapOf(
                "displayName" to "Test User $i",
                "email" to "test_user_$i@test.com",
                "photoUrl" to "",
                "bio" to "",
                "preferredCategories" to emptyList<String>(),
                "favoriteEventIds" to favoriteIds,
                "visitedEventIds" to visitedIds,
                "latitude" to lat,
                "longitude" to lng
            )

            try {
                db.collection("users").document(userId).set(userData).await()
            } catch (e: Exception) {
                Log.e(TAG, "Chyba pri ukladaní užívateľa $userId: ${e.message}")
            }

            // Add user to attendees of each visited event (respect capacity)
            for (eventId in visitedIds) {
                try {
                    val eventDoc = db.collection("events").document(eventId).get().await()
                    if (eventDoc.exists()) {
                        val data = eventDoc.data!!
                        val currentAttendees = (data["attendees"] as? List<*>)?.size ?: 0
                        val maxAttendees = (data["participants"] as? Number)?.toInt() ?: 100

                        if (currentAttendees < maxAttendees) {
                            db.collection("events").document(eventId)
                                .update("attendees", FieldValue.arrayUnion(userId)).await()
                            successfulAdds++
                        } else {
                            capacityReached++
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Chyba pri pridávaní do eventu $eventId: ${e.message}")
                }
            }

            if ((i + 1) % 10 == 0) Log.d(TAG, "Vytvorených ${i + 1}/$count užívateľov")
        }

        Log.d(TAG, "Hotovo! Vytvorených $count užívateľov")
        Log.d(TAG, "Úspešne pridaných do eventov: $successfulAdds, odmietnutých (plná kapacita): $capacityReached")
    }

    // ── Clear test data ───────────────────────────────────────────────────────

    suspend fun clearTestData() {
        Log.d(TAG, "Mažem testovacie dáta...")

        val users = db.collection("users")
            .whereGreaterThanOrEqualTo("__name__", "test_user_")
            .whereLessThan("__name__", "test_user_\uF8FF")
            .get().await()
        users.documents.forEach { it.reference.delete().await() }

        val events = db.collection("events")
            .whereGreaterThanOrEqualTo("__name__", "test_event_")
            .whereLessThan("__name__", "test_event_\uF8FF")
            .get().await()
        events.documents.forEach { it.reference.delete().await() }

        Log.d(TAG, "Testovacie dáta vymazané (${users.size()} užívateľov, ${events.size()} eventov)")
    }
}
