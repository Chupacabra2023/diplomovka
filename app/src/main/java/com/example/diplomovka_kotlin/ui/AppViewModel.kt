package com.example.diplomovka_kotlin.ui

import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomovka_kotlin.data.models.Event
import com.example.diplomovka_kotlin.data.models.Invitation
import com.example.diplomovka_kotlin.data.models.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import android.util.Log
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "AppViewModel"

class AppViewModel : ViewModel() {

    private val db      = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

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
        if (userId in event.attendees) return
        val updated = event.copy(attendees = event.attendees + userId)
        _events[eventId] = updated
        selectedEvent = updated
        viewModelScope.launch {
            try { db.collection("events").document(eventId).update("attendees", updated.attendees).await() }
            catch (_: Exception) {}
        }
    }

    fun leaveEvent(eventId: String, userId: String) {
        val event = _events[eventId] ?: return
        val updated = event.copy(attendees = event.attendees - userId)
        _events[eventId] = updated
        selectedEvent = updated
        viewModelScope.launch {
            try { db.collection("events").document(eventId).update("attendees", updated.attendees).await() }
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

    fun clearInvitationMessages() {
        invitationError = null
        invitationSuccess = null
    }
}
