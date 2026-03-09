package com.example.diplomovka_kotlin.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LandingScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleClick: () -> Unit,
    isLoading: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Vitaj v Zoznamku",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Prihlásiť sa (email/heslo)")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Vytvoriť nový účet")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onGoogleClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
            enabled = !isLoading
        ) {
            Text("Prihlásiť sa cez Google")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
            enabled = !isLoading
        ) {
            Text("Prihlásiť sa cez Facebook")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            enabled = !isLoading
        ) {
            Text("Prihlásiť sa cez Apple")
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}
