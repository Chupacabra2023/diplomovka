package com.example.diplomovka_kotlin.ui

import android.net.Uri
import androidx.compose.runtime.*
import java.util.Date
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomovka_kotlin.data.models.ChatMessage
import com.example.diplomovka_kotlin.data.models.Event
import com.example.diplomovka_kotlin.data.models.Invitation
import com.example.diplomovka_kotlin.data.models.UserProfile
import com.example.diplomovka_kotlin.recommendation.RecommendationEngine
import com.example.diplomovka_kotlin.recommendation.ScoredEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import android.util.Log
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "AppViewModel"

class AppViewModel : ViewModel() {

    private val db      = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // ── Radius + filter stav ──────────────────────────────────────────────────
    var eventRadiusKm by mutableStateOf(10f)
        private set

    fun setEventRadius(km: Float) { eventRadiusKm = km }

    var filterState by mutableStateOf(FilterState())
        private set

    fun updateFilterState(state: FilterState) { filterState = state }

    // ── Events ────────────────────────────────────────────────────────────────
    private val _events = mutableStateMapOf<String, Event>()
    val events: Map<String, Event> = _events

    var selectedEvent by mutableStateOf<Event?>(null)
        private set

    var newEventToAdd by mutableStateOf<Event?>(null)
        private set

    var creatingFromMap = false

    // ── User profile ──────────────────────────────────────────────────────────
    var currentUserProfile by mutableStateOf<UserProfile?>(null)
        private set

    var viewedProfile by mutableStateOf<UserProfile?>(null)
        private set

    fun loadPublicProfile(uid: String) {
        viewedProfile = null
        viewModelScope.launch {
            try {
                val doc = db.collection("users").document(uid).get().await()
                if (doc.exists()) viewedProfile = UserProfile.fromFirestore(uid, doc.data ?: return@launch)
            } catch (e: Exception) { Log.e(TAG, "Chyba načítania profilu: ${e.message}") }
        }
    }

    var isUploadingPhoto by mutableStateOf(false)
        private set

    // ── Invitations ───────────────────────────────────────────────────────────
    private val _invitations = mutableStateListOf<Invitation>()
    val invitations: List<Invitation> = _invitations

    var invitationError by mutableStateOf<String?>(null)
    var invitationSuccess by mutableStateOf<String?>(null)

    init {
        loadEvents()
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            loadUserProfile(it)
            loadInvitations(it)
        }
    }

    // ── Events ────────────────────────────────────────────────────────────────

    private fun loadEvents() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("events").get().await()
                Log.d(TAG, "Načítaných eventov: ${snapshot.size()}")
                snapshot.documents.forEach { doc ->
                    val data = doc.data ?: return@forEach
                    val event = Event.fromFirestore(doc.id, data)
                    _events[event.id] = event
                }
            } catch (e: Exception) {
                Log.e(TAG, "Chyba načítania: ${e.message}")
            }
        }
    }

    fun nextEventId(): String = db.collection("events").document().id

    fun selectEvent(event: Event) { selectedEvent = event }

    fun prepareCreation(event: Event, fromMap: Boolean) {
        selectedEvent = event
        creatingFromMap = fromMap
    }

    fun saveEventToMap(event: Event) {
        _events[event.id] = event
        newEventToAdd = event
        selectedEvent = null
        viewModelScope.launch {
            try {
                db.collection("events").document(event.id).set(event.toMap()).await()
                Log.d(TAG, "Event uložený: ${event.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Chyba uloženia: ${e.message}")
            }
        }
    }

    fun consumeNewEvent() { newEventToAdd = null }

    fun updateEvent(event: Event) {
        _events[event.id] = event
        selectedEvent = event
        viewModelScope.launch {
            try { db.collection("events").document(event.id).set(event.toMap()).await() }
            catch (_: Exception) {}
        }
    }

    fun deleteEvent(eventId: String) {
        _events.remove(eventId)
        selectedEvent = null
        viewModelScope.launch {
            try { db.collection("events").document(eventId).delete().await() }
            catch (_: Exception) {}
        }
    }

    fun joinEvent(eventId: String, userId: String) {
        val event = _events[eventId] ?: return
        if (userId in event.bannedUsers) return
        if (userId in event.attendees || userId in event.waitlist) return
        val isFull = event.participants > 0 && event.attendees.size >= event.participants
        val updated = if (isFull) {
            event.copy(waitlist = event.waitlist + userId)
        } else {
            event.copy(attendees = event.attendees + userId)
        }
        _events[eventId] = updated
        selectedEvent = updated
        viewModelScope.launch {
            try {
                val field = if (isFull) "waitlist" to updated.waitlist else "attendees" to updated.attendees
                db.collection("events").document(eventId).update(field.first, field.second).await()
            } catch (_: Exception) {}
        }
    }

    fun removeAttendee(eventId: String, attendeeId: String) {
        val event = _events[eventId] ?: return
        var updated = event.copy(
            attendees = event.attendees - attendeeId,
            waitlist  = event.waitlist - attendeeId,          // remove from waitlist too
            bannedUsers = event.bannedUsers + attendeeId
        )
        // Promote next from waitlist if there's now a free spot
        if (updated.waitlist.isNotEmpty() &&
            (updated.participants == 0 || updated.attendees.size < updated.participants)) {
            val next = updated.waitlist.first()
            updated = updated.copy(
                attendees = updated.attendees + next,
                waitlist  = updated.waitlist.drop(1)
            )
        }
        _events[eventId] = updated
        selectedEvent = updated
        viewModelScope.launch {
            try {
                db.collection("events").document(eventId).update(
                    mapOf(
                        "attendees"   to updated.attendees,
                        "waitlist"    to updated.waitlist,
                        "bannedUsers" to updated.bannedUsers
                    )
                ).await()
            } catch (_: Exception) {}
        }
    }

    fun leaveEvent(eventId: String, userId: String) {
        val event = _events[eventId] ?: return
        var updated = event.copy(attendees = event.attendees - userId)
        // Automaticky povýš prvého z poradovníka
        if (updated.waitlist.isNotEmpty()) {
            val next = updated.waitlist.first()
            updated = updated.copy(
                attendees = updated.attendees + next,
                waitlist  = updated.waitlist.drop(1)
            )
        }
        _events[eventId] = updated
        selectedEvent = updated
        viewModelScope.launch {
            try {
                db.collection("events").document(eventId).update(
                    mapOf("attendees" to updated.attendees, "waitlist" to updated.waitlist)
                ).await()
            } catch (_: Exception) {}
        }
    }

    fun leaveWaitlist(eventId: String, userId: String) {
        val event = _events[eventId] ?: return
        val updated = event.copy(waitlist = event.waitlist - userId)
        _events[eventId] = updated
        selectedEvent = updated
        viewModelScope.launch {
            try { db.collection("events").document(eventId).update("waitlist", updated.waitlist).await() }
            catch (_: Exception) {}
        }
    }

    // ── User profile ──────────────────────────────────────────────────────────

    fun loadUserProfile(uid: String) {
        viewModelScope.launch {
            try {
                val doc = db.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    currentUserProfile = UserProfile.fromFirestore(uid, doc.data ?: return@launch)
                } else {
                    val authUser = FirebaseAuth.getInstance().currentUser
                    val profile = UserProfile(
                        uid = uid,
                        displayName = authUser?.displayName ?: "",
                        email = authUser?.email ?: "",
                        photoUrl = authUser?.photoUrl?.toString() ?: ""
                    )
                    db.collection("users").document(uid).set(profile.toMap()).await()
                    currentUserProfile = profile
                }
            } catch (e: Exception) {
                Log.e(TAG, "Chyba načítania profilu: ${e.message}")
            }
        }
    }

    fun updateDisplayName(name: String) {
        val profile = currentUserProfile ?: return
        currentUserProfile = profile.copy(displayName = name)
        viewModelScope.launch {
            try { db.collection("users").document(profile.uid).update("displayName", name).await() }
            catch (_: Exception) {}
        }
    }

    fun updateBio(bio: String) {
        val profile = currentUserProfile ?: return
        currentUserProfile = profile.copy(bio = bio)
        viewModelScope.launch {
            try { db.collection("users").document(profile.uid).update("bio", bio).await() }
            catch (_: Exception) {}
        }
    }

    fun togglePreferredCategory(category: String) {
        val profile = currentUserProfile ?: return
        val updated = if (category in profile.preferredCategories)
            profile.preferredCategories - category
        else
            profile.preferredCategories + category
        currentUserProfile = profile.copy(preferredCategories = updated)
        viewModelScope.launch {
            try { db.collection("users").document(profile.uid).update("preferredCategories", updated).await() }
            catch (_: Exception) {}
        }
    }

    fun updateProfilePhoto(uid: String, uri: Uri, onDone: (String) -> Unit) {
        isUploadingPhoto = true
        viewModelScope.launch {
            try {
                val ref = storage.reference.child("users/$uid/avatar.jpg")
                ref.putFile(uri).await()
                val url = ref.downloadUrl.await().toString()
                db.collection("users").document(uid).update("photoUrl", url).await()
                currentUserProfile = currentUserProfile?.copy(photoUrl = url)
                onDone(url)
            } catch (e: Exception) {
                Log.e(TAG, "Chyba nahrávania fotky: ${e.message}")
            } finally {
                isUploadingPhoto = false
            }
        }
    }

    fun toggleFavorite(eventId: String) {
        val profile = currentUserProfile ?: return
        val newFavorites = if (eventId in profile.favoriteEventIds)
            profile.favoriteEventIds - eventId
        else
            profile.favoriteEventIds + eventId
        currentUserProfile = profile.copy(favoriteEventIds = newFavorites)
        viewModelScope.launch {
            try { db.collection("users").document(profile.uid).update("favoriteEventIds", newFavorites).await() }
            catch (_: Exception) {}
        }
    }

    fun isFavorite(eventId: String): Boolean =
        eventId in (currentUserProfile?.favoriteEventIds ?: emptyList())

    // ── Invitations ───────────────────────────────────────────────────────────

    fun loadInvitations(uid: String) {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("invitations")
                    .whereEqualTo("toUserId", uid)
                    .get().await()
                _invitations.clear()
                snapshot.documents.forEach { doc ->
                    val data = doc.data ?: return@forEach
                    _invitations.add(Invitation.fromFirestore(doc.id, data))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Chyba načítania pozvánok: ${e.message}")
            }
        }
    }

    fun sendInvitation(event: Event, fromUserId: String, fromUserName: String, toEmail: String) {
        invitationError = null
        invitationSuccess = null
        viewModelScope.launch {
            try {
                // Nájdi usera podľa emailu
                val userSnap = db.collection("users")
                    .whereEqualTo("email", toEmail.trim().lowercase())
                    .get().await()

                if (userSnap.isEmpty) {
                    invitationError = "Používateľ s týmto emailom neexistuje."
                    return@launch
                }

                val toUserDoc = userSnap.documents.first()
                val toUserId = toUserDoc.id

                if (toUserId == fromUserId) {
                    invitationError = "Nemôžeš pozvať sám seba."
                    return@launch
                }

                // Skontroluj či pozvánka už existuje
                val existing = db.collection("invitations")
                    .whereEqualTo("eventId", event.id)
                    .whereEqualTo("toUserId", toUserId)
                    .get().await()

                if (!existing.isEmpty) {
                    invitationError = "Tento používateľ už bol pozvaný."
                    return@launch
                }

                val docRef = db.collection("invitations").document()
                val invitation = Invitation(
                    id = docRef.id,
                    eventId = event.id,
                    eventTitle = event.title,
                    eventPlace = event.place,
                    fromUserId = fromUserId,
                    fromUserName = fromUserName,
                    toUserId = toUserId,
                    toEmail = toEmail.trim().lowercase()
                )
                docRef.set(invitation.toMap()).await()
                invitationSuccess = "Pozvánka odoslaná!"
            } catch (e: Exception) {
                invitationError = "Chyba: ${e.message}"
                Log.e(TAG, "Chyba odoslania pozvánky: ${e.message}")
            }
        }
    }

    fun respondToInvitation(invitation: Invitation, accept: Boolean, currentUserId: String) {
        val newStatus = if (accept) "accepted" else "declined"
        val updated = invitation.copy(status = newStatus)
        val idx = _invitations.indexOfFirst { it.id == invitation.id }
        if (idx >= 0) _invitations[idx] = updated

        viewModelScope.launch {
            try {
                db.collection("invitations").document(invitation.id)
                    .update("status", newStatus).await()
                if (accept) {
                    joinEvent(invitation.eventId, currentUserId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Chyba odpovede na pozvánku: ${e.message}")
            }
        }
    }

    fun rateEvent(eventId: String, userId: String, stars: Int) {
        val event = _events[eventId] ?: return
        if (userId in event.ratedBy) return                         // already rated
        val newTotal = event.totalRatings + 1
        val newRating = (event.rating * event.totalRatings + stars) / newTotal
        val updated = event.copy(
            rating = newRating,
            totalRatings = newTotal,
            ratedBy = event.ratedBy + userId
        )
        _events[eventId] = updated
        selectedEvent = updated
        viewModelScope.launch {
            try {
                db.collection("events").document(eventId).update(
                    mapOf("rating" to newRating, "totalRatings" to newTotal,
                          "ratedBy" to updated.ratedBy)
                ).await()
            } catch (_: Exception) {}
        }
    }

    fun clearInvitationMessages() {
        invitationError = null
        invitationSuccess = null
    }

    // ── Attendee profiles ─────────────────────────────────────────────────────

    private val _attendeeProfiles = mutableStateMapOf<String, UserProfile>()
    val attendeeProfiles: Map<String, UserProfile> = _attendeeProfiles

    fun loadAttendeeProfiles(attendeeIds: List<String>) {
        viewModelScope.launch {
            for (uid in attendeeIds) {
                if (uid in _attendeeProfiles) continue
                try {
                    val doc = db.collection("users").document(uid).get().await()
                    if (doc.exists()) {
                        _attendeeProfiles[uid] = UserProfile.fromFirestore(uid, doc.data ?: continue)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    // ── Chat ──────────────────────────────────────────────────────────────────

    private val _chatMessages = mutableStateListOf<ChatMessage>()
    val chatMessages: List<ChatMessage> = _chatMessages
    private var chatListener: ListenerRegistration? = null

    fun startChatListener(eventId: String) {
        chatListener?.remove()
        _chatMessages.clear()
        chatListener = db.collection("events").document(eventId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                snapshot ?: return@addSnapshotListener
                _chatMessages.clear()
                snapshot.documents.forEach { doc ->
                    val data = doc.data ?: return@forEach
                    _chatMessages.add(
                        ChatMessage(
                            id = doc.id,
                            senderId = data["senderId"] as? String ?: "",
                            senderName = data["senderName"] as? String ?: "",
                            text = data["text"] as? String ?: "",
                            timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L
                        )
                    )
                }
            }
    }

    fun stopChatListener() {
        chatListener?.remove()
        chatListener = null
        _chatMessages.clear()
    }

    fun sendChatMessage(eventId: String, userId: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val senderName = currentUserProfile?.displayName
            ?: FirebaseAuth.getInstance().currentUser?.displayName ?: "Anonym"
        viewModelScope.launch {
            try {
                db.collection("events").document(eventId)
                    .collection("messages")
                    .add(mapOf(
                        "senderId" to userId,
                        "senderName" to senderName,
                        "text" to trimmed,
                        "timestamp" to System.currentTimeMillis()
                    )).await()
            } catch (_: Exception) {}
        }
    }

    // ── Recommendations ───────────────────────────────────────────────────────

    var recommendedEvents by mutableStateOf<List<ScoredEvent>>(emptyList())
        private set

    var userLatitude by mutableStateOf(48.1486)
        private set
    var userLongitude by mutableStateOf(17.1077)
        private set

    fun updateUserLocation(lat: Double, lng: Double) {
        userLatitude = lat
        userLongitude = lng
    }

    fun refreshRecommendations() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val profile = currentUserProfile

        // Derive visited events from in-memory events (attendee) + profile field
        val visitedFromEvents = _events.values
            .filter { uid in it.attendees }
            .map { it.id }
        val visitedFromProfile = profile?.visitedEventIds ?: emptyList()
        val visitedIds = (visitedFromEvents + visitedFromProfile).distinct()

        val favoriteIds = profile?.favoriteEventIds ?: emptyList()

        val results = RecommendationEngine.getRecommendations(
            userId = uid,
            userLat = userLatitude,
            userLng = userLongitude,
            allEvents = _events.values,
            visitedEventIds = visitedIds,
            favoriteEventIds = favoriteIds,
            maxDistanceKm = eventRadiusKm.toDouble().coerceAtLeast(10.0),
            count = 30
        )
        recommendedEvents = results
    }
}

data class FilterState(
    val name: String = "",
    val category: String = "",
    val subcategory: String = "",
    val participants: Float = 0f,
    val maxPrice: Float = 150f,
    val dateFrom: Date? = null
)
