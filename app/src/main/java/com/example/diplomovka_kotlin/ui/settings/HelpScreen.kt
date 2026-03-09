package com.example.diplomovka_kotlin.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class FaqEntry(val question: String, val answer: String)

@Composable
fun HelpScreen() {
    val context = LocalContext.current
    val faqItems = remember {
        listOf(
            FaqEntry("Ako môžem zmeniť heslo?", "Choď do Nastavení a klikni na Zmeniť heslo."),
            FaqEntry("Ako označím udalosť ako obľúbenú?", "Klikni na hviezdičku pri udalosti."),
            FaqEntry("Ako zruším svoj účet?", "V Nastaveniach dole klikni na Odstrániť účet.")
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Pomoc", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Často kladené otázky", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(faqItems) { faq ->
            FaqCard(faq)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@mojaappka.sk")
                        }
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "Otváram e-mailový klient...", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Kontaktujte nás", fontWeight = FontWeight.Medium)
                    Text("support@mojaappka.sk", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun FaqCard(faq: FaqEntry) {
    var expanded by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(faq.question, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(faq.answer, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
