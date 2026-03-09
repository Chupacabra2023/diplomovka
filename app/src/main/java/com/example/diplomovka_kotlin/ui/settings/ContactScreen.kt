package com.example.diplomovka_kotlin.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBg     = Color(0xFF1A1C1E)
private val DarkAccent = Color(0xFF2D2F31)
private val TextColor  = Color(0xFFE2E2E6)
private val Accent     = Color(0xFFD0BCFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var name    by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor      = Accent,
        unfocusedBorderColor    = Color(0xFF4A4C4F),
        focusedTextColor        = TextColor,
        unfocusedTextColor      = TextColor,
        cursorColor             = Accent,
        focusedLabelColor       = Accent,
        unfocusedLabelColor     = TextColor.copy(alpha = 0.6f),
        focusedContainerColor   = DarkAccent,
        unfocusedContainerColor = DarkAccent
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kontakt", color = TextColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Späť", tint = TextColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkAccent)
            )
        },
        containerColor = DarkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Logo / hlavička ───────────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Email, contentDescription = null,
                        tint = Accent, modifier = Modifier.size(40.dp))
                }
                Text("Sme tu pre teba", color = TextColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Máš otázku, nápad alebo našiel si chybu?\nNapíš nám — odpíšeme čo najskôr.",
                    color = TextColor.copy(alpha = 0.55f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }

            // ── Kontaktné údaje ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkAccent)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ContactRow(
                    icon = Icons.Filled.Email,
                    label = "Email",
                    value = "support@eventapp.sk",
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@eventapp.sk")
                        }
                        context.startActivity(Intent.createChooser(intent, "Odoslať email"))
                    }
                )
                HorizontalDivider(color = TextColor.copy(alpha = 0.07f))
                ContactRow(
                    icon = Icons.Filled.Language,
                    label = "Web",
                    value = "www.eventapp.sk",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://eventapp.sk"))
                        context.startActivity(intent)
                    }
                )
            }

            // ── Formulár ──────────────────────────────────────────────────────
            Text("Správa", color = Accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tvoje meno") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Správa") },
                minLines = 4,
                maxLines = 7,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            Button(
                onClick = {
                    val subject = "Správa od: ${name.ifEmpty { "Anonymný používateľ" }}"
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:support@eventapp.sk")
                        putExtra(Intent.EXTRA_SUBJECT, subject)
                        putExtra(Intent.EXTRA_TEXT, message)
                    }
                    context.startActivity(Intent.createChooser(intent, "Odoslať email"))
                },
                enabled = message.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Accent,
                    contentColor = DarkBg,
                    disabledContainerColor = Accent.copy(alpha = 0.3f),
                    disabledContentColor = DarkBg.copy(alpha = 0.5f)
                )
            ) {
                Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Odoslať správu", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun ContactRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextColor.copy(alpha = 0.5f), fontSize = 11.sp)
            Text(value, color = TextColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        TextButton(onClick = onClick) {
            Text("Otvoriť", color = Accent, fontSize = 12.sp)
        }
    }
}
