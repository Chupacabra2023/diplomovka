package com.example.diplomovka_kotlin.ui.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomovka_kotlin.R
import com.example.diplomovka_kotlin.viewmodel.ResetPasswordViewModel
import kotlinx.coroutines.launch

private val ResetBg     = Color(0xFF1A1C1E)
private val ResetAccent = Color(0xFF2D2F31)
private val ResetText   = Color(0xFFE2E2E6)
private val ResetPurple = Color(0xFFD0BCFF)

@Composable
fun ResetPasswordScreen(
    vm: ResetPasswordViewModel,
    onSuccess: () -> Unit
) {
    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    var email      by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    val scope   = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(error) {
        if (error != null) {
            Toast.makeText(context, "❌ $error", Toast.LENGTH_LONG).show()
            vm.clearError()
        }
    }

    fun validate(): Boolean {
        emailError = if (email.isEmpty() || !email.contains('@')) "Zadajte platný e-mail." else null
        return emailError == null
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor        = ResetText,
        unfocusedTextColor      = ResetText,
        focusedBorderColor      = ResetPurple,
        unfocusedBorderColor    = Color(0xFF4A4C4F),
        focusedLabelColor       = ResetPurple,
        unfocusedLabelColor     = ResetText.copy(alpha = 0.6f),
        cursorColor             = ResetPurple,
        focusedContainerColor   = ResetAccent,
        unfocusedContainerColor = ResetAccent,
        errorBorderColor        = Color(0xFFCF6679),
        errorLabelColor         = Color(0xFFCF6679),
        errorTextColor          = ResetText,
        errorContainerColor     = ResetAccent,
        errorSupportingTextColor = Color(0xFFCF6679)
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

        IconButton(
            onClick = onSuccess,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(4.dp)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Späť", tint = ResetText)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Zabudnuté heslo",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = ResetText
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Zadajte e-mail a pošleme vám odkaz na obnovenie hesla.",
                fontSize = 14.sp,
                color = ResetText.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; emailError = null },
                label = { Text("E-mail") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true,
                colors = fieldColors
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (validate()) scope.launch {
                        if (vm.sendResetEmail(email.trim())) {
                            Toast.makeText(context, "✅ E-mail odoslaný. Skontrolujte schránku!", Toast.LENGTH_LONG).show()
                            onSuccess()
                        }
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ResetAccent,
                    contentColor = ResetText,
                    disabledContainerColor = ResetAccent.copy(alpha = 0.5f),
                    disabledContentColor = ResetText.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .widthIn(min = 200.dp)
                    .height(52.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = ResetPurple
                    )
                } else {
                    Text("Odoslať e-mail", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
