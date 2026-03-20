package com.example.diplomovka_kotlin.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.diplomovka_kotlin.data.models.UserProfile
import java.text.SimpleDateFormat
import java.util.*

private val DarkBg     = Color(0xFF1A1C1E)
private val DarkAcc    = Color(0xFF2D2F31)
private val TextPrim   = Color(0xFFE2E2E6)
private val YellowAcc  = Color(0xFFFFB300)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    profile: UserProfile,
    events: List<Event>,
    onBack: () -> Unit,
    onEventClick: (Event) -> Unit
) {
    val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(profile.displayName.ifEmpty { "Profil" }, color = TextPrim, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Späť", tint = TextPrim)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkAcc)
            )
        },
        containerColor = DarkBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // ── Avatar + meno ─────────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(90.dp).clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profile.photoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = profile.photoUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize().background(DarkAcc),
                                contentAlignment = Alignment.Center
                            ) {
                                val initials = profile.displayName
                                    .split(" ")
                                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                    .take(2).joinToString("")
                                if (initials.isNotEmpty()) {
                                    Text(initials, color = YellowAcc, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Filled.Person, contentDescription = null,
                                        tint = YellowAcc, modifier = Modifier.size(44.dp))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(profile.displayName.ifEmpty { "Neznámy" }, color = TextPrim,
                        fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    if (profile.email.isNotEmpty()) {
                        Text(profile.email, color = TextPrim.copy(alpha = 0.45f), fontSize = 13.sp)
                    }
                }
            }

            // ── Štatistiky ────────────────────────────────────────────────────
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatBox("Udalosti", events.size, Modifier.weight(1f))
                    if (profile.preferredCategories.isNotEmpty()) {
                        StatBox("Záujmy", profile.preferredCategories.size, Modifier.weight(1f))
                    }
                }
            }

            // ── Bio ───────────────────────────────────────────────────────────
            if (profile.bio.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkAcc)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("O mne", color = YellowAcc, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(profile.bio, color = TextPrim, fontSize = 13.sp)
                    }
                }
            }

            // ── Kategórie záujmov ─────────────────────────────────────────────
            if (profile.preferredCategories.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkAcc)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Záujmy", color = YellowAcc, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            profile.preferredCategories.take(6).forEach { cat ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(YellowAcc.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(cat, color = YellowAcc, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // ── Verejné udalosti ──────────────────────────────────────────────
            if (events.isNotEmpty()) {
                item {
                    Text("Verejné udalosti", color = YellowAcc,
                        fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
                items(events, key = { it.id }) { event ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkAcc)
                            .clickable { onEventClick(event) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(event.title, color = TextPrim, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            if (event.place.isNotEmpty()) {
                                Text(event.place, color = TextPrim.copy(alpha = 0.5f), fontSize = 12.sp)
                            }
                        }
                        event.dateFrom?.let {
                            Text(formatter.format(it), color = YellowAcc, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                item {
                    Text("Žiadne verejné udalosti.", color = TextPrim.copy(alpha = 0.4f),
                        fontSize = 13.sp, modifier = Modifier.padding(4.dp))
                }
            }
        }
    }
}

@Composable
private fun StatBox(label: String, count: Int, modifier: Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(DarkAcc).padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(count.toString(), color = YellowAcc, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextPrim.copy(alpha = 0.6f), fontSize = 11.sp)
    }
}
