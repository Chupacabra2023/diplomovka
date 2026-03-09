package com.example.diplomovka_kotlin.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomovka_kotlin.ui.AppViewModel

private val DarkMapBackground = Color(0xFF1A1C1E)
private val DarkAccent        = Color(0xFF2D2F31)
private val PrimaryTextDark   = Color(0xFFE2E2E6)
private val AccentColor       = Color(0xFFD0BCFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinPrivateEventScreen(
    appViewModel: AppViewModel,
    currentUserId: String,
    onBack: () -> Unit,
    onJoined: () -> Unit
) {
    var password     by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var error        by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pripojiť sa cez heslo", color = PrimaryTextDark, fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.Lock,
                contentDescription = null,
                tint = AccentColor,
                modifier = Modifier.size(64.dp)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                "Zadaj heslo súkromného eventu",
                color = PrimaryTextDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; error = null },
                label = { Text("Heslo") },
                isError = error != null,
                supportingText = error?.let { { Text(it, color = Color(0xFFCF6679)) } },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null, tint = AccentColor
                        )
                    }
                },
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

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val event = appViewModel.events.values.find {
                        it.visibility == "private" && it.password == password
                    }
                    if (event == null) {
                        error = "Žiadny event s týmto heslom neexistuje."
                    } else {
                        appViewModel.joinEvent(event.id, currentUserId)
                        appViewModel.selectEvent(event)
                        onJoined()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentColor,
                    contentColor = DarkMapBackground
                )
            ) {
                Text("Vstúpiť", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
