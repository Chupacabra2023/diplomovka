package com.example.diplomovka_kotlin.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.diplomovka_kotlin.data.models.Event
import com.example.diplomovka_kotlin.ui.AppViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

private val DarkBg      = Color(0xFF1A1C1E)
private val DarkAccent  = Color(0xFF2D2F31)
private val TextPrimary = Color(0xFFE2E2E6)
private val Accent      = Color(0xFFD0BCFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    appViewModel: AppViewModel,
    onBack: () -> Unit,
    onEventClick: (Event) -> Unit
) {
    val authUser = FirebaseAuth.getInstance().currentUser
    val uid = authUser?.uid ?: ""
    val profile = appViewModel.currentUserProfile
    val isUploadingPhoto = appViewModel.isUploadingPhoto

    var editingName by remember { mutableStateOf(false) }
    var nameInput   by remember(profile?.displayName) { mutableStateOf(profile?.displayName ?: "") }
    var editingBio  by remember { mutableStateOf(false) }
    var bioInput    by remember(profile?.bio) { mutableStateOf(profile?.bio ?: "") }

    val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { appViewModel.updateProfilePhoto(uid, it) {} }
    }

    val now = remember { Date() }

    val createdEvents = appViewModel.events.values
        .filter { it.creatorId == uid }
        .sortedByDescending { it.createdAt }

    val visitedEvents = appViewModel.events.values
        .filter { event ->
            uid in event.attendees
                && event.creatorId != uid
                && (event.dateTo ?: event.dateFrom)?.before(now) == true
        }
        .sortedByDescending { it.dateFrom }

    val favoriteEvents = appViewModel.events.values
        .filter { appViewModel.isFavorite(it.id) }
        .sortedByDescending { it.createdAt }

    var expandedSection by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Späť", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkAccent)
            )
        },
        containerColor = DarkBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {

            // ── Avatar ────────────────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        val photoUrl = profile?.photoUrl?.takeIf { it.isNotEmpty() }
                            ?: authUser?.photoUrl?.toString()

                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .clickable(enabled = !isUploadingPhoto) { photoPicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (photoUrl != null) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(DarkAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val initials = (profile?.displayName ?: authUser?.displayName ?: "?")
                                        .split(" ")
                                        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                        .take(2).joinToString("")
                                    if (initials.isNotEmpty()) {
                                        Text(initials, color = Accent, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Icon(Icons.Filled.Person, contentDescription = null,
                                            tint = Accent, modifier = Modifier.size(48.dp))
                                    }
                                }
                            }

                            if (isUploadingPhoto) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = Accent,
                                        strokeWidth = 3.dp
                                    )
                                }
                            }
                        }

                        if (!isUploadingPhoto) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Accent)
                                    .clickable { photoPicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = null,
                                    tint = DarkBg, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ── Meno ─────────────────────────────────────────────────
                    if (editingName) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        ) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = fieldColors()
                            )
                            IconButton(onClick = {
                                appViewModel.updateDisplayName(nameInput.trim())
                                editingName = false
                            }) {
                                Icon(Icons.Filled.Check, contentDescription = "Uložiť", tint = Accent)
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                profile?.displayName?.ifEmpty { authUser?.displayName ?: "Hosť" }
                                    ?: authUser?.displayName ?: "Hosť",
                                color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { editingName = true }) {
                                Icon(Icons.Filled.Edit, contentDescription = null,
                                    tint = Accent, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Text(authUser?.email ?: "", color = TextPrimary.copy(alpha = 0.5f), fontSize = 13.sp)
                }
            }

            // ── Štatistiky (klikateľné) ───────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("Vytvorené", createdEvents.size, expandedSection == "created", Modifier.weight(1f)) {
                        expandedSection = if (expandedSection == "created") null else "created"
                    }
                    StatCard("Navštívené", visitedEvents.size, expandedSection == "visited", Modifier.weight(1f)) {
                        expandedSection = if (expandedSection == "visited") null else "visited"
                    }
                    StatCard("Obľúbené", favoriteEvents.size, expandedSection == "favorites", Modifier.weight(1f)) {
                        expandedSection = if (expandedSection == "favorites") null else "favorites"
                    }
                }
            }

            // ── Posledné 3 eventy ─────────────────────────────────────────────
            when (expandedSection) {
                "created" -> {
                    val preview = createdEvents.take(3)
                    if (preview.isEmpty()) {
                        item { EmptyHint("Zatiaľ žiadne vytvorené udalosti.") }
                    } else {
                        items(preview, key = { "c_${it.id}" }) { event ->
                            MiniEventCard(event, formatter) { onEventClick(event) }
                        }
                    }
                }
                "visited" -> {
                    val preview = visitedEvents.take(3)
                    if (preview.isEmpty()) {
                        item { EmptyHint("Zatiaľ žiadna história.") }
                    } else {
                        items(preview, key = { "v_${it.id}" }) { event ->
                            MiniEventCard(event, formatter) { onEventClick(event) }
                        }
                    }
                }
                "favorites" -> {
                    val preview = favoriteEvents.take(3)
                    if (preview.isEmpty()) {
                        item { EmptyHint("Zatiaľ žiadne obľúbené.") }
                    } else {
                        items(preview, key = { it.id }) { event ->
                            MiniEventCard(event, formatter) { onEventClick(event) }
                        }
                    }
                }
            }

            // ── Bio ───────────────────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkAccent)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("O mne", color = Accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        if (!editingBio) {
                            IconButton(
                                onClick = { editingBio = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = null,
                                    tint = Accent, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    if (editingBio) {
                        OutlinedTextField(
                            value = bioInput,
                            onValueChange = { bioInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            placeholder = { Text("Napíš niečo o sebe...", color = TextPrimary.copy(alpha = 0.4f)) },
                            colors = fieldColors()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { bioInput = profile?.bio ?: ""; editingBio = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A4C4F))
                            ) { Text("Zrušiť") }
                            Button(
                                onClick = { appViewModel.updateBio(bioInput.trim()); editingBio = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = DarkBg)
                            ) { Text("Uložiť", fontWeight = FontWeight.Bold) }
                        }
                    } else {
                        Text(
                            profile?.bio?.ifEmpty { "Zatiaľ bez popisu." } ?: "Zatiaľ bez popisu.",
                            color = if (profile?.bio.isNullOrEmpty()) TextPrimary.copy(alpha = 0.4f) else TextPrimary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Accent,
    unfocusedBorderColor = Color(0xFF4A4C4F),
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = Accent,
    focusedContainerColor = Color(0xFF1A1C1E),
    unfocusedContainerColor = Color(0xFF1A1C1E)
)

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, iconTint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
private fun StatCard(label: String, count: Int, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Accent.copy(alpha = 0.2f) else DarkAccent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(count.toString(), color = Accent, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextPrimary.copy(alpha = 0.6f), fontSize = 11.sp)
    }
}

@Composable
private fun EmptyHint(text: String) {
    Text(
        text,
        color = TextPrimary.copy(alpha = 0.4f),
        fontSize = 13.sp,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun MiniEventCard(event: Event, formatter: SimpleDateFormat, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkAccent)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(event.title, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            if (event.place.isNotEmpty()) {
                Text(event.place, color = TextPrimary.copy(alpha = 0.5f), fontSize = 12.sp)
            }
        }
        event.dateFrom?.let {
            Text(formatter.format(it), color = Accent, fontSize = 11.sp)
        }
    }
}
