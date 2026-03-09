package com.example.diplomovka_kotlin.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
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
import java.text.SimpleDateFormat
import java.util.*

private val DarkMapBackground = Color(0xFF1A1C1E)
private val DarkAccent        = Color(0xFF2D2F31)
private val DarkAccent2       = Color(0xFF353739)
private val PrimaryTextDark   = Color(0xFFE2E2E6)
private val AccentColor       = Color(0xFFD0BCFF)

// Legacy stub
data class SimpleEventItem(val title: String, val date: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    title: String,
    events: List<Event>,
    onBack: () -> Unit,
    onEventClick: (Event) -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = PrimaryTextDark, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Späť", tint = PrimaryTextDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkAccent)
            )
        },
        containerColor = DarkMapBackground
    ) { padding ->
        if (events.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        Icons.Filled.History,
                        contentDescription = null,
                        tint = AccentColor.copy(alpha = 0.3f),
                        modifier = Modifier.size(72.dp)
                    )
                    Text("Žiadne udalosti", color = PrimaryTextDark.copy(alpha = 0.4f), fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(events, key = { _, e -> e.id }) { _, event ->
                    EventCard(
                        event = event,
                        dateFormatter = dateFormatter,
                        timeFormatter = timeFormatter,
                        onClick = { onEventClick(event) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EventCard(
    event: Event,
    dateFormatter: SimpleDateFormat,
    timeFormatter: SimpleDateFormat,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkAccent)
            .clickable(onClick = onClick)
    ) {
        // ── Obrázok (ak existuje) ─────────────────────────────────────────────
        if (event.imageUrl.isNotEmpty()) {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )
        }

        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Kategória badge + názov ───────────────────────────────────────
            if (event.category.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        event.category + if (event.subcategory.isNotEmpty()) " · ${event.subcategory}" else "",
                        color = AccentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                event.title,
                color = PrimaryTextDark,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            // ── Miesto ────────────────────────────────────────────────────────
            if (event.place.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null,
                        tint = AccentColor, modifier = Modifier.size(13.dp))
                    Text(event.place, color = PrimaryTextDark.copy(alpha = 0.65f), fontSize = 13.sp)
                }
            }

            HorizontalDivider(color = PrimaryTextDark.copy(alpha = 0.08f))

            // ── Dátum + čas / Cena / Účastníci ───────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                event.dateFrom?.let { date ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = null,
                            tint = AccentColor, modifier = Modifier.size(13.dp))
                        Text(dateFormatter.format(date), color = AccentColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("·", color = PrimaryTextDark.copy(alpha = 0.3f), fontSize = 12.sp)
                        Text(timeFormatter.format(date), color = PrimaryTextDark.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (event.price > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(Icons.Filled.EuroSymbol, contentDescription = null,
                                tint = PrimaryTextDark.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
                            Text("${"%.0f".format(event.price)}", color = PrimaryTextDark.copy(alpha = 0.6f), fontSize = 12.sp)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2A4A2A))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Zadarmo", color = Color(0xFF81C784), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    if (event.attendees.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Filled.People, contentDescription = null,
                                tint = PrimaryTextDark.copy(alpha = 0.4f), modifier = Modifier.size(13.dp))
                            Text("${event.attendees.size}", color = PrimaryTextDark.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
