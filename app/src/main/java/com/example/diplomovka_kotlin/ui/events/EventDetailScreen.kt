package com.example.diplomovka_kotlin.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomovka_kotlin.data.models.Event
import java.text.SimpleDateFormat
import java.util.*

private val DarkMapBackground = Color(0xFF1A1C1E)
private val DarkAccent        = Color(0xFF2D2F31)
private val PrimaryTextDark   = Color(0xFFE2E2E6)
private val AccentColor       = Color(0xFFD0BCFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event,
    currentUserId: String,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onInvite: (String) -> Unit
) {
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val isCreator   = event.creatorId == currentUserId
    val isAttending = currentUserId in event.attendees
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var inviteEmail      by remember { mutableStateOf("") }

    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = { showInviteDialog = false; inviteEmail = "" },
            title = { Text("Pozvať používateľa", color = PrimaryTextDark) },
            text = {
                OutlinedTextField(
                    value = inviteEmail,
                    onValueChange = { inviteEmail = it },
                    label = { Text("Email používateľa") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentColor,
                        unfocusedBorderColor = Color(0xFF4A4C4F),
                        focusedTextColor = PrimaryTextDark,
                        unfocusedTextColor = PrimaryTextDark,
                        cursorColor = AccentColor,
                        focusedContainerColor = DarkAccent,
                        unfocusedContainerColor = DarkAccent
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onInvite(inviteEmail)
                    showInviteDialog = false
                    inviteEmail = ""
                }) {
                    Text("Pozvať", color = AccentColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInviteDialog = false; inviteEmail = "" }) {
                    Text("Zrušiť", color = PrimaryTextDark.copy(alpha = 0.6f))
                }
            },
            containerColor = DarkAccent
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Vymazať udalosť?", color = PrimaryTextDark) },
            text = { Text("Táto akcia je nezvratná.", color = PrimaryTextDark.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Vymazať", color = Color(0xFFCF6679), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Zrušiť", color = AccentColor)
                }
            },
            containerColor = DarkAccent
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event.title, color = PrimaryTextDark, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Späť", tint = PrimaryTextDark)
                    }
                },
                actions = {
                    if (!isCreator) {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Obľúbené",
                                tint = if (isFavorite) Color(0xFFCF6679) else AccentColor
                            )
                        }
                    }
                    if (isCreator) {
                        IconButton(onClick = { showInviteDialog = true }) {
                            Icon(Icons.Filled.PersonAdd, contentDescription = "Pozvať", tint = AccentColor)
                        }
                        IconButton(onClick = onEditClick) {
                            Icon(Icons.Filled.Edit, contentDescription = "Upraviť", tint = AccentColor)
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Vymazať", tint = Color(0xFFCF6679))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkAccent)
            )
        },
        containerColor = DarkMapBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (event.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            DetailCard {
                DetailRow("Miesto", event.place.ifEmpty { "—" })
                DetailRow("Popis", event.description.ifEmpty { "—" })
                DetailRow("Vytvorené", formatter.format(event.createdAt))
                event.dateFrom?.let { DetailRow("Od", formatter.format(it)) }
                event.dateTo?.let { DetailRow("Do", formatter.format(it)) }
                DetailRow("Cena", "${"%.2f".format(event.price)} €")
                DetailRow("Kategória", event.category.ifEmpty { "—" })
            if (event.subcategory.isNotEmpty()) DetailRow("Podkategória", event.subcategory)
                DetailRow("Viditeľnosť", event.visibility)
            }

            DetailCard {
                DetailRow("Tvorca", if (isCreator) "Vy" else event.creatorId.take(8) + "...")
                DetailRow("Počet účastníkov", event.attendees.size.toString())
            }

            if (!isCreator) {
                Spacer(modifier = Modifier.height(8.dp))
                if (isAttending) {
                    Button(
                        onClick = onLeave,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B1A1A),
                            contentColor = PrimaryTextDark
                        )
                    ) {
                        Text("Odísť z eventu", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                } else {
                    Button(
                        onClick = onJoin,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentColor,
                            contentColor = DarkMapBackground
                        )
                    ) {
                        Text("Pripojiť sa", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkAccent)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        content = content
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = AccentColor, fontWeight = FontWeight.Medium, fontSize = 13.sp)
        Text(value, color = PrimaryTextDark, fontSize = 13.sp)
    }
}
