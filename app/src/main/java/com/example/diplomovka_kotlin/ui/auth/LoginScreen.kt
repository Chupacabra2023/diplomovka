package com.example.diplomovka_kotlin.ui.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomovka_kotlin.R
import com.example.diplomovka_kotlin.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

private val LDarkBtn    = Color(0xFF111315)
private val LText       = Color(0xFFE2E2E6)
private val LYellow     = Color(0xFFFFB300)
private val LBorder     = Color(0xFF3A3C3F)

@Composable
fun LoginScreen(
    vm: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onBack: () -> Unit = {}
) {
    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    var email          by remember { mutableStateOf("") }
    var password       by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val scope   = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(error) {
        if (error != null) {
            Toast.makeText(context, "❌ $error", Toast.LENGTH_LONG).show()
            vm.clearError()
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor        = LText,
        unfocusedTextColor      = LText,
        focusedBorderColor      = LYellow,
        unfocusedBorderColor    = LBorder,
        focusedLabelColor       = LYellow,
        unfocusedLabelColor     = LText.copy(alpha = 0.6f),
        cursorColor             = LYellow,
        focusedContainerColor   = LDarkBtn,
        unfocusedContainerColor = LDarkBtn
    )

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
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Nadpis v tmavom boxe
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, LBorder, RoundedCornerShape(20.dp))
                    .background(LDarkBtn)
                    .padding(horizontal = 32.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Prihlásenie",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = LText,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Formulár v tmavej karte
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.5.dp, LBorder, RoundedCornerShape(24.dp))
                    .background(LDarkBtn)
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        singleLine = true,
                        colors = fieldColors
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Heslo") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = LText.copy(alpha = 0.7f)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        singleLine = true,
                        colors = fieldColors
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                if (vm.login(email.trim(), password.trim())) onLoginSuccess()
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E2022),
                            contentColor = LText,
                            disabledContainerColor = Color(0xFF1E2022).copy(alpha = 0.5f),
                            disabledContentColor = LText.copy(alpha = 0.5f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LBorder),
                        modifier = Modifier
                            .widthIn(min = 200.dp)
                            .height(52.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = LYellow
                            )
                        } else {
                            Text("Prihlásiť sa", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    TextButton(onClick = onForgotPasswordClick, enabled = !isLoading) {
                        Text("Zabudol som heslo?", color = LYellow)
                    }
                }
            }
        }

        // Tlačidlo späť — za Column aby bolo nad ním (z-order)
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(4.dp)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Späť", tint = LText)
        }
    }
}
