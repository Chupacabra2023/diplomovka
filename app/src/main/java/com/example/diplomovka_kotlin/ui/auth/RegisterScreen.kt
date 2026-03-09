package com.example.diplomovka_kotlin.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomovka_kotlin.viewmodel.RegisterViewModel
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    vm: RegisterViewModel,
    onRegisterSuccess: () -> Unit
) {
    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(error) {
        if (error != null) {
            Toast.makeText(context, "❌ $error", Toast.LENGTH_LONG).show()
            vm.clearError()
        }
    }

    fun validate(): Boolean {
        emailError = if (email.isEmpty() || !email.contains('@')) "Zadajte platný e-mail." else null
        passwordError = if (password.length < 6) "Heslo musí mať aspoň 6 znakov." else null
        confirmError = when {
            confirmPassword.isEmpty() -> "Potvrdenie hesla je povinné."
            password != confirmPassword -> "Heslá sa nezhodujú."
            else -> null
        }
        return emailError == null && passwordError == null && confirmError == null
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Vytvoriť nový účet", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it; emailError = null },
            label = { Text("E-mail") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(), enabled = !isLoading, singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it; passwordError = null },
            label = { Text("Heslo (min. 6 znakov)") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                }
            },
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(), enabled = !isLoading, singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword, onValueChange = { confirmPassword = it; confirmError = null },
            label = { Text("Potvrď heslo") },
            visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { confirmVisible = !confirmVisible }) {
                    Icon(if (confirmVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                }
            },
            isError = confirmError != null,
            supportingText = confirmError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(), enabled = !isLoading, singleLine = true
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                if (validate()) scope.launch {
                    if (vm.register(email.trim(), password.trim())) {
                        Toast.makeText(context, "✅ Registrácia úspešná! Skontrolujte e-mail.", Toast.LENGTH_LONG).show()
                        onRegisterSuccess()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(), enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            else Text("Registrovať", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
