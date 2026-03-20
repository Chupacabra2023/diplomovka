package com.example.diplomovka_kotlin.ui.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.diplomovka_kotlin.viewmodel.RegisterViewModel
import kotlinx.coroutines.launch

private val RDarkBtn = Color(0xFF111315)
private val RText    = Color(0xFFE2E2E6)
private val RYellow  = Color(0xFFFFB300)
private val RBorder  = Color(0xFF3A3C3F)

@Composable
fun RegisterScreen(
    vm: RegisterViewModel,
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit = {}
) {
    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible  by remember { mutableStateOf(false) }
    var emailError      by remember { mutableStateOf<String?>(null) }
    var passwordError   by remember { mutableStateOf<String?>(null) }
    var confirmError    by remember { mutableStateOf<String?>(null) }

    val scope   = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(error) {
        if (error != null) {
            Toast.makeText(context, "❌ $error", Toast.LENGTH_LONG).show()
            vm.clearError()
        }
    }

    fun validate(): Boolean {
        emailError    = if (email.isEmpty() || !email.contains('@')) "Zadajte platný e-mail." else null
        passwordError = if (password.length < 6) "Heslo musí mať aspoň 6 znakov." else null
        confirmError  = when {
            confirmPassword.isEmpty()   -> "Potvrdenie hesla je povinné."
            password != confirmPassword -> "Heslá sa nezhodujú."
            else                        -> null
        }
        return emailError == null && passwordError == null && confirmError == null
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor         = RText,
        unfocusedTextColor       = RText,
        focusedBorderColor       = RYellow,
        unfocusedBorderColor     = RBorder,
        focusedLabelColor        = RYellow,
        unfocusedLabelColor      = RText.copy(alpha = 0.6f),
        cursorColor              = RYellow,
        focusedContainerColor    = RDarkBtn,
        unfocusedContainerColor  = RDarkBtn,
        errorBorderColor         = Color(0xFFCF6679),
        errorLabelColor          = Color(0xFFCF6679),
        errorTextColor           = RText,
        errorContainerColor      = RDarkBtn,
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(72.dp))

            // Nadpis v tmavom boxe
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, RBorder, RoundedCornerShape(20.dp))
                    .background(RDarkBtn)
                    .padding(horizontal = 28.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Vytvoriť nový účet",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = RText,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Formulár v tmavej karte
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.5.dp, RBorder, RoundedCornerShape(24.dp))
                    .background(RDarkBtn)
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; passwordError = null },
                        label = { Text("Heslo (min. 6 znakov)") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = RText.copy(alpha = 0.7f)
                                )
                            }
                        },
                        isError = passwordError != null,
                        supportingText = passwordError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        singleLine = true,
                        colors = fieldColors
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; confirmError = null },
                        label = { Text("Potvrď heslo") },
                        visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                Icon(
                                    if (confirmVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = RText.copy(alpha = 0.7f)
                                )
                            }
                        },
                        isError = confirmError != null,
                        supportingText = confirmError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        singleLine = true,
                        colors = fieldColors
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (validate()) scope.launch {
                                if (vm.register(email.trim(), password.trim())) {
                                    Toast.makeText(context, "✅ Registrácia úspešná! Skontrolujte e-mail.", Toast.LENGTH_LONG).show()
                                    onRegisterSuccess()
                                }
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E2022),
                            contentColor = RText,
                            disabledContainerColor = Color(0xFF1E2022).copy(alpha = 0.5f),
                            disabledContentColor = RText.copy(alpha = 0.5f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RBorder),
                        modifier = Modifier
                            .widthIn(min = 200.dp)
                            .height(52.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = RYellow
                            )
                        } else {
                            Text("Registrovať", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Tlačidlo späť — musí byť po Column aby bolo nad ním (z-order)
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(4.dp)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Späť", tint = RText)
        }
    }
}
