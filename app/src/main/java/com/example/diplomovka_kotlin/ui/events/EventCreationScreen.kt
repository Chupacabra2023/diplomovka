package com.example.diplomovka_kotlin.ui.events

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.diplomovka_kotlin.BuildConfig
import com.example.diplomovka_kotlin.data.models.CATEGORY_MAP
import com.example.diplomovka_kotlin.data.models.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import java.text.SimpleDateFormat
import java.util.*

private val DarkMapBackground = Color(0xFF1A1C1E)
private val DarkAccent        = Color(0xFF2D2F31)
private val PrimaryTextDark   = Color(0xFFE2E2E6)
private val AccentColor       = Color(0xFFFFB300)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCreationScreen(
    event: Event,
    onBack: () -> Unit,
    onSave: (Event) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    // Nové URI vybrané z galérie
    var selectedUris   by remember { mutableStateOf<List<Uri>>(emptyList()) }
    // Existujúce URL (pri editácii) — môže ich používateľ odstrániť
    var existingUrls   by remember { mutableStateOf(event.imageUrls) }
    var isUploading    by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> selectedUris = selectedUris + uris }

    var title          by remember(event.id) { mutableStateOf(event.title) }
    var description    by remember(event.id) { mutableStateOf(event.description) }
    var place          by remember(event.id) { mutableStateOf(event.place) }
    var price          by remember(event.id) { mutableStateOf(if (event.price > 0) event.price.toString() else "") }
    var participants   by remember(event.id) { mutableStateOf(if (event.participants > 0) event.participants.toString() else "") }
    var dateFrom          by remember(event.id) { mutableStateOf(event.dateFrom) }
    var durationMinutes   by remember(event.id) { mutableStateOf(60) }
    var showDurationMenu  by remember(event.id) { mutableStateOf(false) }
    var selectedCategory    by remember(event.id) { mutableStateOf(event.category) }
    var selectedSubcategory by remember(event.id) { mutableStateOf(event.subcategory) }
    var expandedCategory    by remember { mutableStateOf(false) }
    var expandedSubcategory by remember { mutableStateOf(false) }
    val subcategories = CATEGORY_MAP[selectedCategory] ?: emptyList()
    var isPrivate  by remember { mutableStateOf(event.visibility == "private") }
    var password   by remember { mutableStateOf(event.password) }
    var showPassword by remember { mutableStateOf(false) }

    val durations = listOf(
        30 to "30 minút", 60 to "1 hodina", 90 to "1,5 hodiny",
        120 to "2 hodiny", 180 to "3 hodiny", 240 to "4 hodiny",
        360 to "6 hodín", 480 to "8 hodín", 720 to "12 hodín",
        1440 to "1 deň", 2880 to "2 dni", 4320 to "3 dni", 10080 to "1 týždeň"
    )
    val durationLabel = durations.find { it.first == durationMinutes }?.second ?: "$durationMinutes min"

    fun showDateTimePicker(onSelected: (Date) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(context, { _, y, m, d ->
            TimePickerDialog(context, { _, h, min ->
                cal.set(y, m, d, h, min)
                onSelected(cal.time)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor      = PrimaryTextDark,
        unfocusedTextColor    = PrimaryTextDark,
        focusedBorderColor    = AccentColor,
        unfocusedBorderColor  = Color(0xFF4A4C4F),
        focusedLabelColor     = AccentColor,
        unfocusedLabelColor   = PrimaryTextDark.copy(alpha = 0.6f),
        cursorColor           = AccentColor,
        focusedContainerColor = DarkAccent,
        unfocusedContainerColor = DarkAccent
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vytvorenie udalosti", color = PrimaryTextDark, fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Fotky ────────────────────────────────────────────────────────
            val totalImages = existingUrls.size + selectedUris.size
            if (totalImages == 0) {
                // Prázdny placeholder — klik otvorí galériu
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkAccent)
                        .border(1.dp, Color(0xFF4A4C4F), RoundedCornerShape(12.dp))
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            tint = AccentColor,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text("Pridať fotky", color = PrimaryTextDark.copy(alpha = 0.6f), fontSize = 13.sp)
                    }
                }
            } else {
                // Horizontálny scroll s thumbnailmi
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    // Existujúce URL
                    itemsIndexed(existingUrls) { index, url ->
                        ImageThumbnail(
                            onRemove = { existingUrls = existingUrls.toMutableList().also { it.removeAt(index) } }
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    // Nové vybrané URI
                    itemsIndexed(selectedUris) { index, uri ->
                        ImageThumbnail(
                            onRemove = { selectedUris = selectedUris.toMutableList().also { it.removeAt(index) } }
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    // Tlačidlo pridať ďalšie
                    item {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkAccent)
                                .border(1.dp, Color(0xFF4A4C4F), RoundedCornerShape(10.dp))
                                .clickable { imagePicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Pridať fotku",
                                tint = AccentColor, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }

            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Názov udalosti") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true, colors = fieldColors
            )

            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text("Popis") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3, maxLines = 5, colors = fieldColors
            )

            OutlinedTextField(
                value = place, onValueChange = { place = it },
                label = { Text("Miesto") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true, colors = fieldColors
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = price, onValueChange = { price = it },
                    label = { Text("Cena (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f), singleLine = true, colors = fieldColors
                )
                OutlinedTextField(
                    value = participants, onValueChange = { participants = it },
                    label = { Text("Účastníci") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f), singleLine = true, colors = fieldColors
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { showDateTimePicker { dateFrom = it } },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentColor.copy(alpha = 0.5f))
                ) {
                    Text(dateFrom?.let { formatter.format(it) } ?: "Dátum začiatku", fontSize = 12.sp)
                }

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { showDurationMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentColor),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentColor.copy(alpha = 0.5f))
                    ) {
                        Text(durationLabel, fontSize = 12.sp)
                    }
                    DropdownMenu(
                        expanded = showDurationMenu,
                        onDismissRequest = { showDurationMenu = false }
                    ) {
                        durations.forEach { (minutes, label) ->
                            DropdownMenuItem(
                                text = { Text(label, color = if (minutes == durationMinutes) AccentColor else Color.Unspecified) },
                                onClick = { durationMinutes = minutes; showDurationMenu = false }
                            )
                        }
                    }
                }
            }

            ExposedDropdownMenuBox(expanded = expandedCategory, onExpandedChange = { expandedCategory = it }) {
                OutlinedTextField(
                    value = selectedCategory.ifEmpty { "Vybrať kategóriu" },
                    onValueChange = {}, readOnly = true,
                    label = { Text("Kategória") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = fieldColors
                )
                ExposedDropdownMenu(expanded = expandedCategory, onDismissRequest = { expandedCategory = false }, containerColor = DarkAccent) {
                    CATEGORY_MAP.keys.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat, color = if (cat == selectedCategory) AccentColor else PrimaryTextDark) },
                            onClick = { selectedCategory = cat; selectedSubcategory = ""; expandedCategory = false }
                        )
                    }
                }
            }

            if (selectedCategory.isNotEmpty()) {
                ExposedDropdownMenuBox(expanded = expandedSubcategory, onExpandedChange = { expandedSubcategory = it }) {
                    OutlinedTextField(
                        value = selectedSubcategory.ifEmpty { "Vybrať podkategóriu" },
                        onValueChange = {}, readOnly = true,
                        label = { Text("Podkategória") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubcategory) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = fieldColors
                    )
                    ExposedDropdownMenu(expanded = expandedSubcategory, onDismissRequest = { expandedSubcategory = false }, containerColor = DarkAccent) {
                        subcategories.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub, color = if (sub == selectedSubcategory) AccentColor else PrimaryTextDark) },
                                onClick = { selectedSubcategory = sub; expandedSubcategory = false }
                            )
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(false to "Verejné", true to "Súkromné").forEach { (priv, label) ->
                    val selected = isPrivate == priv
                    Button(
                        onClick = { isPrivate = priv },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) AccentColor else DarkAccent,
                            contentColor   = if (selected) DarkMapBackground else PrimaryTextDark
                        )
                    ) { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) }
                }
            }

            if (isPrivate) {
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Heslo pre vstup") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    visualTransformation = if (showPassword)
                        androidx.compose.ui.text.input.VisualTransformation.None
                    else
                        androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null, tint = AccentColor)
                        }
                    },
                    colors = fieldColors
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    if (title.trim().isEmpty()) {
                        Toast.makeText(context, "Prosím, zadaj názov udalosti.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (description.trim().isEmpty()) {
                        Toast.makeText(context, "Prosím, zadaj popis udalosti.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val computedDateTo = dateFrom?.let { Date(it.time + durationMinutes * 60 * 1000L) }
                    isUploading = true
                    scope.launch {
                        try {
                            var lastError: String? = null
                            val uploadResults = selectedUris.map { uri ->
                                try { uploadToCloudinary(context, uri, event.id) }
                                catch (e: Exception) {
                                    lastError = e.message
                                    android.util.Log.e("EventCreation", "Upload zlyhal: ${e.message}", e)
                                    null
                                }
                            }
                            val uploadedUrls = uploadResults.filterNotNull()
                            val failedCount = uploadResults.count { it == null }
                            if (failedCount > 0) {
                                withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    Toast.makeText(context, "Upload zlyhal: $lastError", Toast.LENGTH_LONG).show()
                                }
                            }

                            onSave(event.copy(
                                title = title.trim(),
                                description = description.trim(),
                                place = place.trim(),
                                price = price.toDoubleOrNull() ?: 0.0,
                                participants = participants.toIntOrNull() ?: 0,
                                dateFrom = dateFrom, dateTo = computedDateTo,
                                visibility = if (isPrivate) "private" else "public",
                                password = if (isPrivate) password else "",
                                category = selectedCategory,
                                subcategory = selectedSubcategory,
                                imageUrls = existingUrls + uploadedUrls
                            ))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Chyba pri ukladaní: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isUploading = false
                        }
                    }
                },
                enabled = !isUploading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor, contentColor = DarkMapBackground)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = DarkMapBackground, strokeWidth = 2.dp)
                } else {
                    Text("Uložiť udalosť", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

private suspend fun uploadToCloudinary(
    context: android.content.Context,
    uri: android.net.Uri,
    eventId: String
): String = withContext(Dispatchers.IO) {
    val cloudName    = BuildConfig.CLOUDINARY_CLOUD_NAME
    val uploadPreset = BuildConfig.CLOUDINARY_UPLOAD_PRESET

    val boundary = "----FormBoundary${System.currentTimeMillis()}"
    val url = URL("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
    val conn = (url.openConnection() as HttpsURLConnection).apply {
        requestMethod = "POST"
        doOutput = true
        connectTimeout = 30_000
        readTimeout = 60_000
        setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
    }

    val bytes = context.contentResolver.openInputStream(uri)!!.readBytes()

    DataOutputStream(conn.outputStream).use { out ->
        fun field(name: String, value: String) {
            out.writeBytes("--$boundary\r\n")
            out.writeBytes("Content-Disposition: form-data; name=\"$name\"\r\n\r\n")
            out.writeBytes("$value\r\n")
        }
        field("upload_preset", uploadPreset)
        out.writeBytes("--$boundary\r\n")
        out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"\r\n")
        out.writeBytes("Content-Type: image/jpeg\r\n\r\n")
        out.write(bytes)
        out.writeBytes("\r\n--$boundary--\r\n")
    }

    val responseCode = conn.responseCode
    val response = if (responseCode in 200..299) {
        conn.inputStream.bufferedReader().readText()
    } else {
        val error = conn.errorStream?.bufferedReader()?.readText() ?: "HTTP $responseCode"
        throw Exception("Cloudinary error $responseCode: $error")
    }
    JSONObject(response).getString("secure_url")
}

@Composable
private fun ImageThumbnail(
    onRemove: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .size(110.dp)
            .clip(RoundedCornerShape(10.dp))
    ) {
        content()
        // X tlačidlo
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color(0xCC1A1C1E))
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Odstrániť",
                tint = Color.White, modifier = Modifier.size(14.dp))
        }
    }
}
