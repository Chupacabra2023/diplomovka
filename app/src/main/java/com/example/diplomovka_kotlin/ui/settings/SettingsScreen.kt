package com.example.diplomovka_kotlin.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomovka_kotlin.ui.AppViewModel

@Composable
fun SettingsScreen(appViewModel: AppViewModel, onLogoutClick: () -> Unit) {
    var darkMode by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf("Slovensky") }
    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        val languages = listOf("Slovensky", "English", "Deutsch")
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Vyber jazyk") },
            text = {
                Column {
                    languages.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedLanguage = lang; showLanguageDialog = false }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage == lang,
                                onClick = { selectedLanguage = lang; showLanguageDialog = false }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(lang)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Nastavenia", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSwitch(
            label = "Tmavý režim",
            checked = darkMode,
            onCheckedChange = { darkMode = it }
        )

        HorizontalDivider()

        SettingsSwitch(
            label = "Notifikácie",
            checked = notifications,
            onCheckedChange = { notifications = it }
        )

        HorizontalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showLanguageDialog = true }
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Jazyk", fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(selectedLanguage, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(Icons.Filled.ChevronRight, contentDescription = null)
            }
        }

        HorizontalDivider()

        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Okruh zobrazenia eventov", fontSize = 16.sp)
                Text("${appViewModel.eventRadiusKm.toInt()} km",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Slider(
                value = appViewModel.eventRadiusKm,
                onValueChange = { appViewModel.setEventRadius(it) },
                valueRange = 1f..50f
            )
        }

        HorizontalDivider()

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Odhlásiť sa")
        }
    }
}

@Composable
private fun SettingsSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 16.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
