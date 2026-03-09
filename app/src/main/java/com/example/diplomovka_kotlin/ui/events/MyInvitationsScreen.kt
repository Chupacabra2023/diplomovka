package com.example.diplomovka_kotlin.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomovka_kotlin.data.models.Invitation
import com.example.diplomovka_kotlin.ui.AppViewModel
import com.google.firebase.auth.FirebaseAuth

private val DarkBg     = Color(0xFF1A1C1E)
private val DarkAccent = Color(0xFF2D2F31)
private val TextColor  = Color(0xFFE2E2E6)
private val Accent     = Color(0xFFD0BCFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyInvitationsScreen(
    appViewModel: AppViewModel,
    onBack: () -> Unit
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val invitations = appViewModel.invitations
    val pending  = invitations.filter { it.status == "pending" }
    val answered = invitations.filter { it.status != "pending" }

    // Feedback snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val error   = appViewModel.invitationError
    val success = appViewModel.invitationSuccess

    LaunchedEffect(error, success) {
        val msg = error ?: success ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        appViewModel.clearInvitationMessages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pozvánky", color = TextColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Späť", tint = TextColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkAccent)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBg
    ) { padding ->
        if (invitations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Filled.Mail, contentDescription = null,
                        tint = Accent.copy(alpha = 0.3f), modifier = Modifier.size(72.dp))
                    Text("Žiadne pozvánky", color = TextColor.copy(alpha = 0.4f), fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (pending.isNotEmpty()) {
                    item {
                        Text("Čakajúce", color = Accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    items(pending, key = { it.id }) { inv ->
                        InvitationCard(
                            invitation = inv,
                            onAccept = { appViewModel.respondToInvitation(inv, true, currentUserId) },
                            onDecline = { appViewModel.respondToInvitation(inv, false, currentUserId) }
                        )
                    }
                }

                if (answered.isNotEmpty()) {
                    item {
                        Text("Vybavené", color = TextColor.copy(alpha = 0.5f),
                            fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                            modifier = Modifier.padding(top = 8.dp))
                    }
                    items(answered, key = { "a_${it.id}" }) { inv ->
                        InvitationCard(invitation = inv, onAccept = null, onDecline = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun InvitationCard(
    invitation: Invitation,
    onAccept: (() -> Unit)?,
    onDecline: (() -> Unit)?
) {
    val isPending  = invitation.status == "pending"
    val isAccepted = invitation.status == "accepted"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkAccent)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Hlavička ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(invitation.eventTitle, color = TextColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)

                if (invitation.eventPlace.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null,
                            tint = Accent, modifier = Modifier.size(12.dp))
                        Text(invitation.eventPlace, color = TextColor.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                }
            }

            // Status badge
            if (!isPending) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isAccepted) Color(0xFF2A4A2A) else Color(0xFF4A2A2A))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        if (isAccepted) "Prijaté" else "Odmietnuté",
                        color = if (isAccepted) Color(0xFF81C784) else Color(0xFFCF6679),
                        fontSize = 11.sp, fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // ── Od koho ───────────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Filled.Person, contentDescription = null,
                tint = TextColor.copy(alpha = 0.4f), modifier = Modifier.size(13.dp))
            Text("Pozval: ${invitation.fromUserName.ifEmpty { invitation.fromUserId.take(8) }}",
                color = TextColor.copy(alpha = 0.5f), fontSize = 12.sp)
        }

        // ── Tlačidlá (len pre pending) ────────────────────────────────────────
        if (isPending && onAccept != null && onDecline != null) {
            HorizontalDivider(color = TextColor.copy(alpha = 0.08f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCF6679)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCF6679).copy(alpha = 0.5f))
                ) { Text("Odmietnuť", fontSize = 13.sp) }

                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = DarkBg)
                ) { Text("Prijať", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}
