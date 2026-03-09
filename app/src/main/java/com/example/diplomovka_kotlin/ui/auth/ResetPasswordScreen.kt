package com.example.diplomovka_kotlin.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomovka_kotlin.viewmodel.ResetPasswordViewModel
import kotlinx.coroutines.launch

@Composable
fun ResetPasswordScreen(
    vm: ResetPasswordViewModel,
    onSuccess: () -> Unit
) {
    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
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
        return emailError == null
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Zabudnuté heslo", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it; emailError = null },
            label = { Text("E-mail") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(), enabled = !isLoading, singleLine = true
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                if (validate()) scope.launch {
                    if (vm.sendResetEmail(email.trim())) {
                        Toast.makeText(context, "✅ E-mail odoslaný. Skontrolujte schránku!", Toast.LENGTH_LONG).show()
                        onSuccess()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(), enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            else Text("Odoslať e-mail", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
