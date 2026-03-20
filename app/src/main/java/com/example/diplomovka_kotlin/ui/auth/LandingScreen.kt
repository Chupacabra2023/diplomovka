package com.example.diplomovka_kotlin.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomovka_kotlin.R

private val DarkBtn    = Color(0xFF111315)   // tmavší button
private val LandingText = Color(0xFFE2E2E6)
private val LandingYellow = Color(0xFFFFB300)
private val BorderColor = Color(0xFF3A3C3F)

@Composable
fun LandingScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleClick: () -> Unit,
    isLoading: Boolean = false
) {
    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(R.drawable.login_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1C1E).copy(alpha = 0.65f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp)
        ) {

            // "Vitajte v Joinly" v tmavom krúžku (zaoblený obdĺžnik)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.5.dp, BorderColor, RoundedCornerShape(24.dp))
                    .background(DarkBtn)
                    .padding(horizontal = 28.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Vitajte v",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        color = LandingText,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Joinly",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = LandingYellow,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLoginClick,
                enabled = !isLoading,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBtn,
                    contentColor = LandingText,
                    disabledContainerColor = DarkBtn.copy(alpha = 0.5f),
                    disabledContentColor = LandingText.copy(alpha = 0.5f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                modifier = Modifier
                    .widthIn(min = 220.dp)
                    .height(52.dp)
            ) {
                Text("Prihlásiť sa", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Button(
                onClick = onRegisterClick,
                enabled = !isLoading,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBtn,
                    contentColor = LandingText,
                    disabledContainerColor = DarkBtn.copy(alpha = 0.5f),
                    disabledContentColor = LandingText.copy(alpha = 0.5f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                modifier = Modifier
                    .widthIn(min = 220.dp)
                    .height(52.dp)
            ) {
                Text("Vytvoriť nový účet", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            // Google G v krúžku — biele G
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, BorderColor, CircleShape)
                    .background(DarkBtn)
                    .clickable(enabled = !isLoading) { onGoogleClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "G",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator(color = LandingYellow)
            }
        }
    }
}
